package ast.types;

import java.util.Objects;

public class PointerType implements Type{

    private Type baseType;

    public PointerType(Type baseType) {
        this.baseType = baseType;
    }

    public Type getBaseType() {
        return baseType;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointerType that = (PointerType) o;
        return Objects.equals(baseType, that.baseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseType, "Pointer");
    }

    @Override
    public Type copy() {
        try {
            return (Type) clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
