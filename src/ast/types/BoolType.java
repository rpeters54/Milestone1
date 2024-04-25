package ast.types;

public class BoolType implements Type {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BoolType;
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
