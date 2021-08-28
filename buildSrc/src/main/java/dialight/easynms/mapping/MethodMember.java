package dialight.easynms.mapping;

import org.jetbrains.annotations.Nullable;

public class MethodMember extends Member {

    public String srcDesc;
    public @Nullable String dstDesc;

    public MethodMember(String src, String dst, String srcDesc, @Nullable String dstDesc) {
        super(src, dst);
        this.srcDesc = srcDesc;
        this.dstDesc = dstDesc;
    }

    public MethodMember(String src, String dst, String srcDesc) {
        this(src, dst, srcDesc, null);
    }

    @Override
    public String getSrcDesc() {
        return srcDesc;
    }

    @Override
    public void setSrcDesc(String srcDesc) {
        this.srcDesc = srcDesc;
    }

    @Override
    public @Nullable String getDstDesc() {
        return dstDesc;
    }

    @Override
    public void setDstDesc(@Nullable String dstDesc) {
        this.dstDesc = dstDesc;
    }

    @Override
    public String getSrcId() {
        return src + srcDesc;
    }

    public static void assertDstDesc(MethodMember that) {
        if (that.dstDesc == null)
            throw new IllegalStateException("MethodMember.dstDesc is undefined. Use Mapping.resolveDstDesc or Member.resolveDstDesc(Mapping) to fix");
    }

    @Override
    public String getDstId() {
        assertDstDesc(this);
        return dst + dstDesc;
    }

    @Override
    public void accept(ClassVisitor visitor) {
        visitor.visitMethod(this.src, this.dst, this.srcDesc, this.dstDesc);
    }

    @Override
    public Member copy() {
        return new MethodMember(src, dst, srcDesc, dstDesc);
    }

}
