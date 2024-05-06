package ast;

import instructions.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class BasicBlock {
    private Label label;
    private final ArrayDeque<Instruction> contents;
    private final List<BasicBlock> children;
    private final List<BasicBlock> parents;

    // milestone 3 stuff
    private final Map<String, Source> localBindings;    //local symbol table for ssa
    private boolean unsealed;                           //boolean for if block is unsealed

    // printing stuff
    private final List<String> visitorList;
    private final int sernum;
    private boolean written;

    private static int instanceCount = 0;

    public BasicBlock() {
        this.label = null;
        this.contents = new ArrayDeque<>();
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
        this.localBindings = new HashMap<>();

        this.unsealed = false;

        // printing stuff
        this.visitorList = new ArrayList<>();
        this.written = false;
        this.sernum = instanceCount++;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public boolean isUnsealed() {
        return unsealed;
    }

    public void seal() {
        unsealed = false;
    }

    public void unseal() {
        unsealed = true;
    }

    public Queue<Instruction> getContents() {
        return contents;
    }

    public List<BasicBlock> getChildren() {
        return children;
    }

    public List<BasicBlock> getParents() {
        return parents;
    }

    public Map<String, Source> getLocalBindings() {
        return localBindings;
    }

    public Source lookupLocalBinding(String name) {
        return localBindings.get(name);
    }

    public void addLocalBinding(String name, Source source) {
        localBindings.put(name, source);
    }

    public String getName() {
        return "N" + sernum;
    }

    public Label getLabel() {
        return label;
    }

    public void addCode(Instruction inst) {
        contents.add(inst);
    }

    public void addChild(BasicBlock child) {
        children.add(child);
        child.parents.add(this);
    }

    public boolean endsWithJump() {
        if (contents.size() == 0) {
            return false;
        }
        return contents.getLast() instanceof JumpInstruction;
    }

    // copies local bindings from another block
    public void copyBindings(BasicBlock other) {
        Map<String, Source> otherBindings = other.getLocalBindings();
        for (String key : otherBindings.keySet()) {
            Source item = otherBindings.get(key).copy();
            item.setLabel(this.label);
            this.localBindings.put(key, item);
        }
    }

    //combine bindings from prior blocks to populate next blocks
    public void reconcileBranch(BasicBlock left, BasicBlock right) {
        Map<String, Source> rightBindings = right.getLocalBindings();
        Map<String, Source> leftBindings = left.getLocalBindings();
        Set<String> keySet = new HashSet<>(rightBindings.keySet());
        keySet.addAll(leftBindings.keySet());
        for (String id : keySet) {
            List<Source> sourceList = this.searchPredecessors(id);
            if (sourceList.size() == 1) {
                this.addLocalBinding(id, sourceList.get(0));
            } else {
                Register phiReg = Register.genTypedLocalRegister(sourceList.get(0).getType(), this.label);
                PhiInstruction phi = new PhiInstruction(id, phiReg, sourceList);
                this.addCode(phi);
                this.addLocalBinding(id, phiReg);
            }
        }
    }

    public List<Source> searchPredecessors(String id) {
        Map<Integer, Boolean> visitedMap = new HashMap<>();
        Map<Integer, List<Source>> occurrenceMap = new HashMap<>();
        List<Source> allOccurrences = new ArrayList<>();


        Source item = localBindings.get(id);
        if (item != null) {
            List<Source> baseList = new ArrayList<>();
            baseList.add(item.copy());
            occurrenceMap.put(this.sernum, baseList);
        }

        visitedMap.put(this.sernum, true);
        for (BasicBlock parent : parents) {
            List<Source> parentOccurrences = parent.searchPredHelper(id, occurrenceMap, visitedMap);
            if (parentOccurrences == null) {
                throw new IllegalArgumentException("BasicBlock: Value must be defined at some point");
            }
            for (Source source : parentOccurrences) {
                source.setLabel(parent.getLabel());
            }
            allOccurrences.addAll(parentOccurrences);
        }
        return allOccurrences;
    }

    public List<Source> searchPredHelper(String id, Map<Integer, List<Source>> occurrenceMap, Map<Integer, Boolean> visitedMap) {
        // check if already defined
        List<Source> allOccurrences = occurrenceMap.get(this.sernum);
        if (allOccurrences != null) {
            return allOccurrences;
        }

        // check if this predecessor has already been visited
        Boolean visited = visitedMap.get(this.sernum);
        if (visited != null) {
            return new ArrayList<>();
        }
        visitedMap.put(this.sernum, true);

        // otherwise check if the item is locally defined
        allOccurrences = new ArrayList<>();
        Source item = localBindings.get(id);
        if (item != null) {
            allOccurrences.add(item.copy());
        } else {
            // if not locally defined search its parents
            for (BasicBlock parent : parents) {
                List<Source> parentOccurrences = parent.searchPredHelper(id, occurrenceMap, visitedMap);
                if (parentOccurrences == null) {
                    throw new IllegalArgumentException("BasicBlock: Value must be defined at some point");
                }
                allOccurrences.addAll(parentOccurrences);
            }
        }
        occurrenceMap.put(sernum, allOccurrences);
        return allOccurrences;
    }


    public void toDotFile(String filename) {
        resetTraversal();
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
        written = true;
        writer.write(String.format("\t%s [label=\"", getName()));
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
            if (!child.written) {
                child.writeLabels(writer);
            }
        }
    }

    public void writeGraph(FileWriter writer) throws IOException {
        for (BasicBlock child : children) {
            if (!child.visitorList.contains(getName())) {
                writer.write(String.format("\t%s -> %s\n", getName(), child.getName()));
                child.writeGraph(writer);
                child.visitorList.add(getName());
            }
        }
    }

    private void resetTraversal() {
        written = false;
        for (BasicBlock child : children) {
            if (child.written) {
                child.resetTraversal();
            }
        }
    }

}
