package ast;

import ast.types.*;
import instructions.FunctionStub;
import instructions.Register;

import java.util.HashMap;
import java.util.Map;

public class LLVMEnvironment {
    private int identifier;
    private Map<String, FunctionStub> functions;
    private Map<String, Register> globalBindings;
    private Map<String, Register> localBindings;
    private Map<String, TypeDeclaration> typeDecls;
    private String retLabel;


    public LLVMEnvironment() {
        this.identifier = 1;
        this.functions = new HashMap<>();
        this.globalBindings = new HashMap<>();
        this.localBindings = new HashMap<>();
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

    public void addFunctionStub(String id, FunctionStub func) {
        functions.put(id, func);
    }

    public void addGlobalBinding(String id, Register reg) {
        globalBindings.put(id, reg);
    }

    public void addLocalBinding(String id, Register reg) {
        localBindings.put(id, reg);
    }

    public void addTypeDeclaration(String s, TypeDeclaration td) {
        typeDecls.put(s, td);
    }

    public FunctionStub lookupFunction(String s) {
        return functions.get(s);
    }

    public Register lookupReg(String s) {
        Register reg = localBindings.get(s);
        if (reg == null) {
            reg = globalBindings.get(s);
        }
        return reg;
    }

    public TypeDeclaration lookupTypeDeclaration(String s) {
        return typeDecls.get(s);
    }


    public String getNextBranch() {
        int temp = identifier;
        identifier += 1;
        return String.format("%d", temp);
    }

    public String getRetLabel() {
        return retLabel;
    }


}
