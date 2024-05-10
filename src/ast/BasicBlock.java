package ast;

import ast.types.NullType;
import ast.types.Type;
import instructions.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BasicBlock {
    private Label label;
    private final Deque<Instruction> contents;
    private final List<BasicBlock> children;
    private final List<BasicBlock> parents;

    // milestone 3 stuff
    private final Map<String, Source> localBindings;    //local symbol table for ssa
    private boolean unsealed;                           //boolean for if block is unsealed

    // printing stuff
    private final int sernum;

    private static int instanceCount = 0;

    public BasicBlock() {
        this.label = null;
        this.contents = new ArrayDeque<>();
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
        this.localBindings = new HashMap<>();

        this.unsealed = false;

        // printing stuff
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

    public Deque<Instruction> getContents() {
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

    public void removeChildren() {
        for (BasicBlock child : children) {
            child.parents.remove(this);
        }
        children.clear();
    }

    public boolean endsWithJump() {
        if (contents.size() == 0) {
            return false;
        }
        return contents.getLast() instanceof JumpInstruction;
    }


    //combine bindings from prior blocks to populate next blocks
    public void reconcileBranch(BasicBlock left, BasicBlock right) {
        Map<String, Source> rightBindings = right.getLocalBindings();
        Map<String, Source> leftBindings = left.getLocalBindings();
        Set<String> keySet = new HashSet<>(rightBindings.keySet());
        keySet.addAll(leftBindings.keySet());
        for (String id : keySet) {
            List<PhiTuple> sourceList = this.searchPredecessors(id);
            if (sourceList.size() == 1) {
                this.addLocalBinding(id, sourceList.get(0).getSource().copy());
            } else {
                Register phiReg = Register.genTypedLocalRegister(sourceList.get(0).getType().copy(), this.label);
                PhiInstruction phi = new PhiInstruction(id, phiReg, sourceList);
                this.addCode(phi);
                this.addLocalBinding(id, phiReg);
            }
        }
    }

    public List<PhiTuple> searchPredecessors(String id) {
        Map<Integer, Boolean> visitedMap = new HashMap<>();
        Map<Integer, List<Source>> occurrenceMap = new HashMap<>();
        List<PhiTuple> allValues = new ArrayList<>();


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
                throw new IllegalArgumentException("SearchPredecessor: Value must be defined at some point");
            }
//            if (parentOccurrences.size() != 1) {
//                throw new RuntimeException("SearchPredecessor: Parent block should only provide one value");
//            }
            List<PhiTuple> tuples = parentOccurrences.stream()
                    .map(member -> new PhiTuple(member, parent.getLabel()))
                    .collect(Collectors.toList());
            allValues.addAll(tuples);
        }
        return allValues;
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


}
