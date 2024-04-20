package ast.types;

public class NullType implements Type {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof NullType;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
