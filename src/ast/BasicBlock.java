package ast;

import instructions.*;
import instructions.Instruction;
import instructions.llvm.JumpInstruction;
import instructions.llvm.PhiLLVMInstruction;

import java.util.*;
import java.util.stream.Collectors;

public class BasicBlock {
    private Label label;
    private final Deque<Instruction> contents;
    private final List<BasicBlock> children;
    private final List<BasicBlock> parents;

    //local symbol table for ssa
    private final Map<String, Source> localBindings;

    // used for aggressive deadcode elimination
    private boolean deathmark;

    // printing stuff
    private final int sernum;
    private static int instanceCount = 0;

    public BasicBlock() {
        this.label = null;
        this.contents = new ArrayDeque<>();
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
        this.localBindings = new HashMap<>();

        this.deathmark = true;

        // printing stuff
        this.sernum = instanceCount++;
    }

    public void setLabel(Label label) {
        this.label = label;
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


    // helpers for per-block environments
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


    // deathmark update/query for dead code elim
    public void markCritical() {
        deathmark = false;
    }

    public void markDead() {
        deathmark = true;
    }

    public boolean isDead() {
        return deathmark;
    }

    public void addCode(Instruction inst) {
        contents.add(inst);
    }

    public void addChild(BasicBlock child) {
        children.add(child);
        child.parents.add(this);
    }

    public void removeParents() {
        for (BasicBlock parent : parents) {
            parent.children.remove(this);
        }
        parents.clear();
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

    /*
     * compute the reverse dominance frontier of a given basic block
     * returns a list of basic blocks that define it
     */
    public List<BasicBlock> computeRDF() {
        Set<BasicBlock> visitedSet = new HashSet<>();

        List<BasicBlock> frontier = new ArrayList<>();
        for (BasicBlock parent : parents) {
            frontier.addAll(parent.computeRDFHelper(this, visitedSet));
        }
        return frontier;
    }

    public List<BasicBlock> computeRDFHelper(BasicBlock guard, Set<BasicBlock> visitedSet) {
        // create a new reverse dominance frontier list
        List<BasicBlock> frontier = new ArrayList<>();

        // check if already visited
        // if not, mark as visited and continue
        if (visitedSet.contains(this))
            return frontier;
        visitedSet.add(this);

        // if the current block has a path to the end, it is a part of the frontier
        // its parents will not be, just return it
        if (this.pathToEnd(guard)) {
            frontier.add(this);
            return frontier;
        }

        // if the current block is not part of the frontier, its parents might be
        // build the frontier list by recursing
        for (BasicBlock parent : parents) {
            frontier.addAll(parent.computeRDFHelper(guard, visitedSet));
        }

        return frontier;
    }

    /**
     * Determines whether there exists a path to the epilogue
     * from 'this' that does not contain 'guard'
     * returns a boolean given this result
     */
    public boolean pathToEnd(BasicBlock guard) {
        Set<BasicBlock> visitedSet = new HashSet<>();
        boolean check = false;
        for (BasicBlock child : children) {
            check |= child.pathToEndHelper(guard, visitedSet);
        }
        return check;
    }

    public boolean pathToEndHelper(BasicBlock guard, Set<BasicBlock> visitedSet) {
        // avoid loops by checking if the block has been visited
        if (visitedSet.contains(this))
            return false;

        // mark current block as visited
        visitedSet.add(this);

        // hit the guard, return false
        if (this.equals(guard)) {
            return false;
        }

        // reached the end, return true;
        if (this.children.size() == 0) {
            return true;
        }

        // if not at the end or the guard, recurse
        boolean retVal = false;
        for (BasicBlock child : children) {
            retVal |= child.pathToEndHelper(guard, visitedSet);
        }
        return retVal;
    }

    /**
     * Finds the nearest post-dominator to 'this'
     */
    public BasicBlock computeNearestPostDominator() {
        Set<BasicBlock> visitedSet = new HashSet<>();
        Queue<BasicBlock> blockQueue = new ArrayDeque<>(children);
        // mark current block as visited
        visitedSet.add(this);
        while (!blockQueue.isEmpty()) {
            BasicBlock block = blockQueue.poll();
            // if the block has been visited, ignore it
            if (visitedSet.contains(block))
                continue;

            // mark the block as visited
            visitedSet.add(block);

            // if 'this' has a path to the end that does not
            // pass through block it is not post-dominated
            boolean unobstructed = this.pathToEnd(block);
            // if the block post-dominates 'this' and is
            // not set to be removed, add it
            if (!unobstructed && !block.deathmark) {
                return block;
            }

            // otherwise, add the block's children
            blockQueue.addAll(block.children);
        }
        // ideally this should never be reached as the epilogue
        // postdominates all blocks by default
        throw new RuntimeException("Should have found post dominator");
    }

    /**
     * Generates a queue of BasicBlocks in postorder
     */
    public Queue<BasicBlock> computePostorder() {
        Set<BasicBlock> visitedSet = new HashSet<>();
        Queue<BasicBlock> postorderQueue = new ArrayDeque<>();
        computePostorderHelper(postorderQueue, visitedSet);
        return postorderQueue;
    }

    public void computePostorderHelper(Queue<BasicBlock> postOrderQueue, Set<BasicBlock> visitedSet) {
        if (visitedSet.contains(this))
            return;
        visitedSet.add(this);

        for (BasicBlock child : children) {
            child.computePostorderHelper(postOrderQueue, visitedSet);
        }
        postOrderQueue.add(this);
    }



    /**
     * Given a if-statement, the if and else case may generate new bindings
     * This function create phi instructions for the end-case
     */
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
                Register phiReg = Register.genTypedLocalRegister(sourceList.get(0).getType().copy(), this.getLabel());
                PhiLLVMInstruction phi = new PhiLLVMInstruction(id, phiReg, sourceList);
                this.addCode(phi);
                this.addLocalBinding(id, phiReg);
            }
        }
    }

    /**
     * Search a block's predecessors for bindings mapping to a specific id
     * return a list of all of them so that a phi can be generated
     */
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
