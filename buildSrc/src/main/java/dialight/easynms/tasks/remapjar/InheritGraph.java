package dialight.easynms.tasks.remapjar;

import dialight.easynms.mapping.Mapping;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Stream;

public class InheritGraph {

    private static class ConcurrentNode {

        final Deque<ConcurrentNode> parents = new ConcurrentLinkedDeque<>();
        final Deque<ConcurrentNode> children = new ConcurrentLinkedDeque<>();
        String name;

        private ConcurrentNode(String name) {
            this.name = name;
        }

    }
    private Map<String, ConcurrentNode> index = new ConcurrentHashMap<>();

    private InheritGraph() {}

    public static class Pair implements Comparable<Pair> {
        public final String first;
        public final String second;

        public Pair(String first, String second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public int compareTo(Pair o) {
            return (this.first + this.second).compareTo(o.first + o.second);
        }
    }

    public Stream<Pair> getPairs() {
        return this.index.entrySet().stream().map(e -> {
            final var concurrentNode = e.getValue().parents.peekFirst();
            return new Pair(e.getKey(), concurrentNode != null ? concurrentNode.name : null);
        });
    }

    private void collectSupersHelper(List<String> supers, ConcurrentNode node) {
        for (ConcurrentNode parent : node.parents) {
            supers.add(parent.name);
            collectSupersHelper(supers, parent);
        }
    }
    public List<String> collectSuperClasses(String className, boolean includeCurrent) {
        final var node = index.get(className);
        if(node == null) return includeCurrent ? Collections.singletonList(className) : Collections.emptyList();
        final var supers = new ArrayList<String>();
        if(includeCurrent) supers.add(node.name);
        collectSupersHelper(supers, node);
        return supers;
    }

    public void remap(Mapping mapping) {
        final var newIndex = new HashMap<String, ConcurrentNode>();
        for (ConcurrentNode node : this.index.values()) {
            final var clazz = mapping.getClass(node.name);
            if(clazz != null) node.name = clazz.dst;
            newIndex.put(node.name, node);
        }
        this.index = newIndex;
    }

    private void handleInherit(String name, String superName) {
        final var currentNode = index.computeIfAbsent(name, ConcurrentNode::new);
        final var superNode = index.computeIfAbsent(superName, ConcurrentNode::new);
        currentNode.parents.add(superNode);
        superNode.children.add(currentNode);
    }

    public static InheritGraph build(ArrayList<RemapClassItem> classFiles) {
        final var graph = new InheritGraph();
        classFiles.parallelStream().forEach(remapClassItem -> {
            try {
                try (final var is = Files.newInputStream(remapClassItem.readFile)) {
                    final var reader = new ClassReader(is);
                    reader.accept(new ClassVisitor(Opcodes.ASM9) {
                        @Override
                        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                            if(superName != null && !superName.startsWith("java/lang/")) graph.handleInherit(name, superName);
                            for (String inf : interfaces) {
                                graph.handleInherit(name, inf);
                            }
                        }
                    }, 0);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return graph;
    }
    
}
