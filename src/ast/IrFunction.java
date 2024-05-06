package ast;

import ast.types.Type;
import instructions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class IrFunction {
    private final IrProgram parent;
    private final Function definition;
    private final Map<String, Register> localBindings;   //local symbol table
    private final BasicBlock body;

    private final Queue<BasicBlock> preorderQueue;


    public IrFunction(IrProgram parent, Function func) {
        this.parent = parent;
        parent.addFunction(func.getName(), func);
        this.definition = func;
        this.body = new BasicBlock();
        this.localBindings = new HashMap<>();
        this.preorderQueue = new ArrayDeque<>();
        preorderQueue.add(body);
    }

    public Register lookupReg(String s) {
        Register reg = localBindings.get(s);
        if (reg == null) {
            reg = parent.getGlobalBindings().get(s);
        }
        return reg;
    }

    public boolean isBound(String id) {
        for (Declaration param : definition.concatDecls()) {
            if (param.getName().equals(id)) {
                return true;
            }
        }
        return false;
    }


    public Register lookupGlobal(String s) {
        return parent.getGlobalBindings().get(s);
    }

    public BasicBlock getBody() {
        return body;
    }

    public Queue<BasicBlock> getPreorderQueue() {
        return preorderQueue;
    }

    public TypeDeclaration lookupTypeDeclaration(String s) {
        return parent.getTypeDecls().get(s);
    }

    public Function lookupFunction(String name) {
        return parent.getFunction().get(name);
    }


    public void addLocalBinding(String name, Register reg) {
        localBindings.put(name, reg);
    }

    public void addToQueue(BasicBlock block) {
        preorderQueue.add(block);
    }




    /* Printing Code */

    public void toLLFile(FileWriter writer) {
        Queue<BasicBlock> topQ = new ArrayDeque<>(preorderQueue);
        writeHeader(writer);
        for (int i = 0; i < topQ.size(); i++) {
            BasicBlock next = topQ.poll();
            writeContents(writer, next.getLabel(), next.getContents());
            topQ.add(next);
        }
        writeEOF(writer);
    }

    public void writeHeader(FileWriter writer) {
        String stubStart = String.format("define %s @%s(", TypeMap.ttos(definition.getRetType()), definition.getName());
        StringBuilder stubBuilder = new StringBuilder(stubStart);
        int before = stubBuilder.length();
        int regNum = 0;
        for (Declaration param : definition.getParams()) {
            stubBuilder.append(String.format("%s %%r%d, ", TypeMap.ttos(param.getType()), regNum++));
        }
        if (before != stubBuilder.length()) {
            stubBuilder.delete(stubBuilder.length() - 2, stubBuilder.length());
        }
        stubBuilder.append(") {");
        try {
            writer.write(stubBuilder.toString());
            writer.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeContents(FileWriter writer, Label label, Queue<Instruction> contents) {
        try {
            writer.write(String.format("%s\n", label.toString()));
            for (Instruction code : contents) {
                writer.write(String.format("%s\n", code.toString()));
            }
            writer.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeEOF(FileWriter writer) {
        try {
            writer.write("}\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
