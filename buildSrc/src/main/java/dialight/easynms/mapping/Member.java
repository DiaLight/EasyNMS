package dialight.easynms.mapping;

import dialight.easynms.mapping.ClassVisitor;
import org.jetbrains.annotations.Nullable;

public abstract class Member {

    public final String src;
    public String dst;

    public Member(String src, String dst) {
        this.src = src;
        this.dst = dst;
    }

    public abstract String getSrcDesc();
    public abstract void setSrcDesc(String srcDesc);
    public abstract @Nullable String getDstDesc();
    public abstract void setDstDesc(@Nullable String dstDesc);

    public abstract String getSrcId();
    public abstract String getDstId();

    public abstract void accept(ClassVisitor visitor);

    public abstract Member copy();

}
