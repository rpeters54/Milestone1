package ast;

import javax.imageio.IIOException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class BasicBlock {
    private List<String> contents;
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

    public List<String> getContents() {
        return contents;
    }

    public List<BasicBlock> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public void addCode(String s) {
        contents.add(s);
    }

    public void addChild(BasicBlock child) {
        children.add(child);
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
        StringBuilder sb = new StringBuilder("");
        for (String code : contents) {
            char[] str = code.toCharArray();
            for (int i = 0; i < str.length; i++) {
                switch (str[i]) {
                    case '{' -> {str[i] = '[';}
                    case '}' -> {str[i] = ']';}
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
            for (String code : contents) {
                writer.write(String.format("%s\n", code));
            }
            writer.write("\n");
            Queue<BasicBlock> basicBlockQueue = new ArrayDeque<>(children);
            while (!basicBlockQueue.isEmpty()) {
                BasicBlock block = basicBlockQueue.poll();
                block.wroteLabel = true;
                block.dumpContents(writer, basicBlockQueue);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dumpContents(FileWriter writer, Queue<BasicBlock> basicBlockQueue) throws IOException {
        for (String code : contents) {
            writer.write(String.format("%s\n", code));
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
