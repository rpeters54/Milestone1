package ast;

import instructions.Instruction;
import instructions.Register;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class IrProgram {
    private final List<Instruction> header;                 //instructions defining a program header
    private final List<IrFunction> functions;               //list of functions
    private final Map<String, Register> globalBindings;     //global symbol table
    private final Map<String, Function> functionStubs;         //function definition table
    private final Map<String, TypeDeclaration> typeDecls;   //type declaration table

    public IrProgram() {
        this.header = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.globalBindings = new HashMap<>();
        this.typeDecls = new HashMap<>();
        this.functionStubs = new HashMap<>();
    }

    public Map<String, Register> getGlobalBindings() {
        return globalBindings;
    }

    public Map<String, TypeDeclaration> getTypeDecls() {
        return typeDecls;
    }

    public Map<String, Function> getFunction() {
        return functionStubs;
    }

    public void addToHeader(Instruction inst) {
        header.add(inst);
    }

    public void addIrFunction(IrFunction func) {
        functions.add(func);
    }

    public void addTypeDeclaration(TypeDeclaration typeDecl) {
        typeDecls.put(typeDecl.getName(), typeDecl);
    }

    public void addGlobalBinding(String name, Register reg) {
        globalBindings.put(name, reg);
    }

    public void addFunction(String name, Function func) {
        functionStubs.put(name, func);
    }


    public void toDotFile(String filename) {
        String name = "Header";
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write("digraph \"CFG\" {\n");
            writer.write("\tnode [shape=record];\n");
            writeLabels(writer, name, header);
            for (IrFunction func : functions) {
                BasicBlock head = func.getPreorderQueue().peek();
                writeGraph(writer, name, head.getName());
                for (BasicBlock block : func.getPreorderQueue()) {
                    writeLabels(writer, block.getName(), new ArrayList<>(block.getContents()));
                    for (BasicBlock child : block.getChildren()) {
                        writeGraph(writer, block.getName(), child.getName());
                    }
                }
            }
            writer.write("}");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeLabels(FileWriter writer, String name, List<Instruction> instructionList) throws IOException {
        writer.write(String.format("\t%s [label=\"", name));
        StringBuilder sb = new StringBuilder();
        for (Instruction code : instructionList) {
            char[] str = code.toString().toCharArray();
            for (int i = 0; i < str.length; i++) {
                switch (str[i]) {
                    case '{' -> str[i] = '[';
                    case '}' -> str[i] = ']';
                    case '\"' -> str[i] = '\'';
                }
            }
            sb.append(String.valueOf(str));
            sb.append("\\n ");
        }
        if (sb.length() > 0) {
            sb.delete(sb.length()-3, sb.length());
        }
        writer.write(sb.toString());
        writer.write("\"];\n");
    }

    public void writeGraph(FileWriter writer, String parent, String child) throws IOException {
        writer.write(String.format("\t%s -> %s\n", parent, child));
    }

    public void toLLFile(String filename) {
        try {
            FileWriter writer = new FileWriter(filename);
            for (Instruction code : header) {
                writer.write(String.format("%s\n", code.toString()));
            }
            writer.write("\n");
            for (IrFunction func : functions) {
                func.toLLFile(writer);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
