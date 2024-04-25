package ast.types;

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
    public Type copy() {
        try {
            return (Type) clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
