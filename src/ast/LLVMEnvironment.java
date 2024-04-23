package ast;

import ast.types.*;
import instructions.LocalRegister;

import java.util.HashMap;
import java.util.Map;

public class LLVMEnvironment {
    private int identifier;
    private Map<String, Couple> globalBindings;
    private Map<String, Couple> localBindings;
    private Map<Type, String> structTypeMap;
    private Map<String, TypeDeclaration> typeDecls;
    private String retLabel;


    private static class Couple {
        private final String reg;
        private final Type type;

        private Couple(Type type, String reg) {
            this.type = type;
            this.reg = reg;
        }
    }

    public LLVMEnvironment() {
        this.identifier = 1;
        this.globalBindings = new HashMap<>();
        this.localBindings = new HashMap<>();
        this.structTypeMap = new HashMap<>();
        this.typeDecls = new HashMap<>();
        this.retLabel = "retLabel";
    }

    public void refreshLocals() {
        this.localBindings = new HashMap<>();
        this.identifier = 1;
    }

    public void updateRetLabel() {
        retLabel = getNextBranch();
    }

    public void addGlobalBinding(String s, Type t, String id) {
        globalBindings.put(s, new Couple(t, "@"+id));
    }

    public void addLocalBinding(String s, Type t, String id) {
        localBindings.put(s, new Couple(t, id));
    }

    public void addTypeDeclaration(String s, TypeDeclaration td) {
        typeDecls.put(s, td);
        structTypeMap.put(new StructType(-1, td.getName()),
                String.format("%%struct.%s*", td.getName()));
    }

    public String getNextReg() {
        int temp = identifier;
        identifier += 1;
        return String.format("%%%d", temp);
    }

    public String getNextBranch() {
        int temp = identifier;
        identifier += 1;
        return String.format("%d", temp);
    }

    public String getRetLabel() {
        return retLabel;
    }

    public Type lookupTypeBinding(String s) {
        Couple c = localBindings.get(s);
        if (c == null) {
            return globalBindings.get(s).type;
        }
        return c.type;
    }

    public String lookupRegBinding(String s) {
        Couple c = localBindings.get(s);
        if (c == null) {
            return globalBindings.get(s).reg;
        }
        return c.reg;
    }

    public Type getRegType(String reg) {
        for (Map.Entry<String, Couple> entry : localBindings.entrySet()) {
            Couple val = entry.getValue();
            if (val.reg.equals(reg)) {
                return val.type;
            }
        }
        for (Map.Entry<String, Couple> entry : globalBindings.entrySet()) {
            Couple val = entry.getValue();
            if (val.reg.equals(reg)) {
                return val.type;
            }
        }
        throw new IllegalArgumentException("'reg' is not defined in local and global space");
    }

    public boolean isLocal(String s) {
        return localBindings.get(s) != null;
    }

    public TypeDeclaration lookupTypeDeclaration(String s) {
        return typeDecls.get(s);
    }

    public String typeToString(Type type) {
        if (type instanceof IntType) {
            return "i64";
        } else if (type instanceof ArrayType){
            return "i64*";
        } else if (type instanceof BoolType) {
            return "i1";
        } else if (type instanceof VoidType) {
            return "void";
        } else if (type instanceof NullType) {
            return "i64*";
        } else if (type instanceof PointerType){
            PointerType p = (PointerType) type;
            return typeToString(p.getBaseType())+"*";
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
