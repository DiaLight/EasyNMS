package dialight.easynms.tasks;

import dialight.easynms.mapping.Clazz;
import dialight.easynms.mapping.Mapping;
import dialight.easynms.mapping.Member;
import dialight.easynms.mapping.io.AnotherMappingFormatWriter;
import dialight.easynms.mapping.io.ProguardReader;
import dialight.easynms.mapping.utils.Mappings;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BuildMappings extends DefaultTask {

    @InputFile
    abstract public Property<Path> getInputClassMapping();

    @InputFile
    abstract public Property<Path> getInputMembersMapping();

    @InputFile
    abstract public Property<Path> getInputMojangMapping();

    @OutputFile
    abstract public RegularFileProperty getOutputMapping();

    @OutputFile
    abstract public RegularFileProperty getOutputInverseMapping();

    @TaskAction
    public void action() throws IOException {
        final var inClassMapFile = getInputClassMapping().get();
        final var inMembersMapFile = getInputMembersMapping().get();
        final var inMojangMapFile = getInputMojangMapping().get();
        final var outMappingFile = getOutputMapping().get().getAsFile().toPath();
        final var outInverseMappingFile = getOutputInverseMapping().get().getAsFile().toPath();

        Mapping spigot = new Mapping();
        try(BufferedReader br = Files.newBufferedReader(inClassMapFile)) {
            br.lines()
                    .filter(line -> !line.startsWith("#"))
                    .map(line -> line.split(" "))
                    .forEach(cols -> {
                        spigot.visitClass(cols[1], cols[0]);
                    });
        }

        try(BufferedReader br = Files.newBufferedReader(inMembersMapFile)) {
            br.lines()
                    .filter(line -> !line.startsWith("#"))
                    .map(line -> line.split(" "))
                    .forEach(cols -> {
                        final var clazz = spigot.requireClass(cols[0]);
                        final var src = cols[3];
                        final var dst = cols[1];
                        final var srcDesc = cols[2];
                        if(srcDesc.contains("(")) {
                            clazz.visitMethod(src, dst, srcDesc, null);
                        } else {
                            clazz.visitField(src, dst, srcDesc, null);
                        }
                    });
        }

        Mappings.fixMissingDstDesc(spigot);  // spigot -> notch

        final var mojang = new Mapping();

        mojang.visitStart();
        try(final var reader = Files.newBufferedReader(inMojangMapFile);
            final var parser = new ProguardReader(mojang)) {
            reader.lines().forEach(parser::accept);
        }
        mojang.visitEnd();

        Mappings.fixMissingDstDesc(mojang);  // mojang -> notch

        final var inverseSpigot = Mappings.inverseCopy(spigot);  // notch -> spigot
        final var inverseMojang = Mappings.inverseCopy(mojang);  // notch -> mojang


        final var inverseOutput = Mappings.copy(mojang);  // mojang -> notch
        customApplyToDst(inverseOutput, inverseSpigot, inverseMojang);  // mojang -> (spigot or notch) in mojang scope
//        Mappings.applyToDst(inverseOutput, mojang);  // mojang -> (spigot or mojang) in mojang scope

        final var output = Mappings.inverseCopy(inverseOutput);  // (spigot or notch) -> mojang


        try(final var bw = Files.newBufferedWriter(outMappingFile)) {
            final var anotherMappingFormatWriter = new AnotherMappingFormatWriter(line -> {
                try {
                    bw.write(line);bw.newLine();
                } catch (IOException e) { throw new RuntimeException(e); }
            });
            output.accept(anotherMappingFormatWriter);
        }

        try(final var bw = Files.newBufferedWriter(outInverseMappingFile)) {
            final var anotherMappingFormatWriter = new AnotherMappingFormatWriter(line -> {
                try {
                    bw.write(line);bw.newLine();
                } catch (IOException e) { throw new RuntimeException(e); }
            });
            inverseOutput.accept(anotherMappingFormatWriter);
        }

    }

    public static void customApplyToDst(Mapping mapping, Mapping inverseSpigot, Mapping inverseMojang) {
        for (Clazz clazz : mapping.getClasses()) {  // this.srcToKey
            if(clazz.src.equals("net/minecraft/data/info/BlockListReport")) {
                System.out.println();
            }

            Clazz modifierClass = inverseSpigot.getClass(clazz.dst);
            if(modifierClass == null) {
                boolean remapped = false;
                int $idx = clazz.dst.indexOf('$');
                if($idx != -1) {  // inner class
                    modifierClass = inverseSpigot.getClass(clazz.dst.substring(0, $idx));
                    if(modifierClass != null) {
                        clazz.dst = modifierClass.dst + clazz.dst.substring($idx);
                        remapped = true;
                    }
                }
                if(!remapped) clazz.dst = clazz.src;
                modifierClass = null;
            } else {
                clazz.dst = modifierClass.dst;
            }
            for (Member member : clazz.getMembers()) {  // this.srcToKey
                Member modifierMember = modifierClass != null ? modifierClass.getMember(member.getDstId()) : null;
                if (modifierMember != null) {
                    member.dst = modifierMember.dst;
                    final var modifierDstDesc = modifierMember.getDstDesc();
                    if(modifierDstDesc != null) {
                        member.setDstDesc(modifierDstDesc);
                        continue;
                    }
                }
                String dstDesc = member.getDstDesc();
                if (dstDesc == null) dstDesc = Mappings.remapDesc(mapping, member.getSrcDesc());
                dstDesc = Mappings.remapDesc(inverseSpigot, dstDesc);
//                dstDesc = Mappings.remapDesc(inverseMojang, dstDesc);
                member.setDstDesc(dstDesc);
            }
        }
    }

}
