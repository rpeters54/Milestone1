package ast.types;

public class PointerType implements Type{

    private Type baseType;

    public PointerType(Type baseType) {
        this.baseType = baseType;
    }

    public Type getBaseType() {
        return baseType;
    }
}
