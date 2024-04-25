package ast.types;

public class VoidType implements Type {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof VoidType;
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
