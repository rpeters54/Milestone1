package ast;

import ast.types.Type;

public class Value {
    private Type type;
    private String irType;
    private String value;


    public Value(LLVMEnvironment env, Type type, String value) {
        this.type = type;
        this.irType = env.typeToString(type);
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getIrType() {
        return irType;
    }

    public String getValue() {
        return value;
    }

    public void updateValue(String value) {
        this.value = value;
    }

    public void updateType(LLVMEnvironment env, Type type) {
        this.type = type;
        this.irType = env.typeToString(type);
    }
}
