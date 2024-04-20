package ast;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
    private List<String> contents;
    private List<BasicBlock> children;

    public BasicBlock() {
        this.contents = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public List<String> getContents() {
        return contents;
    }

    public List<BasicBlock> getChildren() {
        return children;
    }

    public void addCode(String s) {
        contents.add(s);
    }

    public void addChild(BasicBlock child) {
        children.add(child);
    }
}
