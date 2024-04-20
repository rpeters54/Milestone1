package ast;

import ast.types.Type;

public class LLVMMetadata {
    private Type type;
    String inlineType;
    private String value;


    public LLVMMetadata(Type type, String inlineType, String value) {
        this.type = type;
        this.inlineType = inlineType;
        this.value = value;
    }

    public LLVMMetadata(Type type, String inlineType, int reg) {
        this.type = type;
        this.inlineType = inlineType;
        this.value = String.format("%%%d", reg);
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getInlineType() {
        return inlineType;
    }
}
