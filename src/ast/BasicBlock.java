package ast;

import instructions.Instruction;
import instructions.JumpInstruction;

import javax.imageio.IIOException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class BasicBlock {
    private List<Instruction> contents;
    private List<BasicBlock> children;
    private String name;

    private boolean wroteLabel;
    private List<String> visitorList;
    private static int instance = 0;

    public BasicBlock() {
        this.contents = new ArrayList<>();
        this.children = new ArrayList<>();
        this.wroteLabel = false;
        this.visitorList = new ArrayList<>();
        this.name = "N"+instance;
        instance++;
    }

    public List<Instruction> getContents() {
        return contents;
    }

    public List<BasicBlock> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public void addCode(Instruction inst) {
        contents.add(inst);
    }

    public void addChild(BasicBlock child) {
        children.add(child);
    }


    public boolean endsWithJump() {
        return contents.get(contents.size()-1) instanceof JumpInstruction;
    }

    public void toDotFile(String filename) {
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write("digraph \"CFG\" {\n");
            writer.write("\tnode [shape=record];\n");
            writeLabels(writer);
            writeGraph(writer);
            writer.write("}");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeLabels(FileWriter writer) throws IOException {
        wroteLabel = true;
        writer.write(String.format("\t%s [label=\"", name));
        StringBuilder sb = new StringBuilder();
        for (Instruction code : contents) {
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
        for (BasicBlock child : children) {
            if (!child.wroteLabel) {
                child.writeLabels(writer);
            }
        }
    }

    public void writeGraph(FileWriter writer) throws IOException {
        for (BasicBlock child : children) {
            if (!child.visitorList.contains(name)) {
                writer.write(String.format("\t%s -> %s\n", name, child.name));
                child.writeGraph(writer);
                child.visitorList.add(name);
            }
        }
    }

    public void toLLFile(String filename) {
        try {
            FileWriter writer = new FileWriter(filename);
            wroteLabel = true;
            for (Instruction code : contents) {
                writer.write(String.format("%s\n", code.toString()));
            }
            writer.write("\n");
            for (BasicBlock child : children) {
                Queue<BasicBlock> basicBlockQueue = new ArrayDeque<>();
                child.wroteLabel = true;
                child.dumpContents(writer, basicBlockQueue);
                while(!basicBlockQueue.isEmpty()) {
                    basicBlockQueue.poll().dumpContents(writer, basicBlockQueue);
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dumpContents(FileWriter writer, Queue<BasicBlock> basicBlockQueue) throws IOException {
        for (Instruction code : contents) {
            writer.write(String.format("%s\n", code.toString()));
        }
        writer.write("\n");
        for (BasicBlock child : children) {
            if (!child.wroteLabel) {
                basicBlockQueue.add(child);
                child.wroteLabel = true;
            }
        }
    }


}
