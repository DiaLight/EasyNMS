package dialight.easynms.mapping;

import dialight.easynms.mapping.ClassVisitor;
import dialight.easynms.mapping.FieldMember;
import dialight.easynms.mapping.Member;
import dialight.easynms.mapping.MethodMember;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Clazz implements ClassVisitor {

    public final String src;
    public String dst;
    public final Map<String, Member> members = new HashMap<>();

    public Clazz(String src, String dst) {
        this.src = src;
        this.dst = dst;
    }

    @Override
    public void visitField(String src, String dst, String srcDesc, String dstDesc) {
        FieldMember member = new FieldMember(src, dst, srcDesc, dstDesc);
        members.put(member.getSrcId(), member);
    }

    @Override
    public void visitMethod(String src, String dst, String srcDesc, String dstDesc) {
        Member member = new MethodMember(src, dst, srcDesc, dstDesc);
        members.put(member.getSrcId(), member);
    }

    public void accept(ClassVisitor visitor) {
        for (Member member : this.members.values()) {
            member.accept(visitor);
        }
    }

    public void addMember(Member member) {
        members.put(member.getSrcId(), member);
    }

    public Collection<Member> getMembers() {
        return members.values();
    }

    @Nullable
    public Member getMember(String dst) {
        return members.get(dst);
    }

    @Nullable
    public MethodMember getMethod(String name, String desc) {
        return (MethodMember) members.get(name + desc);
    }

    @Nullable
    public FieldMember getField(String name) {
        return (FieldMember) members.get(name);
    }

}
