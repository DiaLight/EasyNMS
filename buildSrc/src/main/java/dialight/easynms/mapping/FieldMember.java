package dialight.easynms.mapping;

import dialight.easynms.mapping.ClassVisitor;
import dialight.easynms.mapping.Member;
import org.jetbrains.annotations.Nullable;

public class FieldMember extends Member {

    public @Nullable String srcDesc;
    public @Nullable String dstDesc;

    public FieldMember(String src, String dst, @Nullable String srcDesc, @Nullable String dstDesc) {
        super(src, dst);
        this.srcDesc = srcDesc;
        this.dstDesc = dstDesc;
    }

    public FieldMember(String src, String dst) {
        this(src, dst, null, null);
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
        return src;
    }

    @Override
    public String getDstId() {
        return dst;
    }

    @Override
    public void accept(ClassVisitor visitor) {
        visitor.visitField(this.src, this.dst, this.srcDesc, this.dstDesc);
    }

    @Override
    public Member copy() {
        return new FieldMember(src, dst, srcDesc, dstDesc);
    }

}
