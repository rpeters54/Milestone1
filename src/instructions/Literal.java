package instructions;

import ast.types.Type;

public class Literal implements Source {
    private Type type;
    private String value;

    public Literal(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getTypeString() {
        return TypeMap.ttos(type);
    }
}
