package ast;

import ast.types.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LLVMEnvironment {
    private int currentRegister;
    private Map<String, Type> bindings;
    private Map<Type, String> structTypeMap;
    private Map<String, TypeDeclaration> typeDecls;


    public LLVMEnvironment() {
        this.currentRegister = 0;
        this.bindings = new HashMap<>();
        this.structTypeMap = new HashMap<>();
        this.typeDecls = new HashMap<>();
    }

    public LLVMEnvironment copy() {
        LLVMEnvironment newLL = new LLVMEnvironment();
        newLL.bindings.putAll(this.bindings);
        newLL.typeDecls.putAll(this.typeDecls);
        return newLL;
    }

    public void addBinding(String s, Type t) {
        bindings.put(s, t);
    }

    public void addTypeDeclaration(String s, TypeDeclaration td) {
        typeDecls.put(s, td);
        structTypeMap.put(new StructType(-1, td.getName()),
                String.format("%%struct.%s*", td.getName()));
    }

    public int getCurrentRegister() {
        return currentRegister++;
    }

    public Type lookupBinding(String s) {
        return bindings.get(s);
    }

    public TypeDeclaration lookupTypeDeclaration(String s) {
        return typeDecls.get(s);
    }

    public String typeToString(Type type) {
        if (type instanceof IntType) {
            return "i64";
        } else if (type instanceof BoolType) {
            return "i1";
        } else if (type instanceof VoidType) {
            return "void";
        } else if (type instanceof NullType) {
            return "i64*";
        } else if (type instanceof FunctionType) {
            FunctionType ft = (FunctionType) type;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(String.format("%s (", typeToString(ft.getOutput())));
            for (Type in : ft.getInputs()) {
                stringBuilder.append(String.format("%s, ", typeToString(in)));
            }
            stringBuilder.delete(stringBuilder.length()-2, stringBuilder.length());
            stringBuilder.append(")");
            return stringBuilder.toString();
        } else if (type instanceof StructType) {
            return structTypeMap.get(type);
        } else {
            throw new IllegalArgumentException("Non-Existent Type");
        }
    }
}
