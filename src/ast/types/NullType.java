package ast.types;

import java.util.Objects;

public class NullType implements Type {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof NullType
                || obj instanceof StructType
                || obj instanceof ArrayType;
    }

    @Override
    public int hashCode() {
        return Objects.hash("Null");
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
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
