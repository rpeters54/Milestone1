package ast.types;

import java.util.Objects;

public class IntType implements Type {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof IntType;
    }

    @Override
    public int hashCode() {
        return Objects.hash("Int");
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
