package ast;

import ast.declarations.Declaration;
import ast.types.Type;
import ast.declarations.TypeDeclaration;
import instructions.*;
import instructions.llvm.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class IrFunction {
    private final IrProgram parent;
    private final Function definition;
    private final Map<String, Register> localBindings;   //local symbol table
    private final Map<Register, Instruction> criticalMap;

    private final Deque<BasicBlock> preorderQueue;


    public IrFunction(IrProgram parent, Function func) {
        this.parent = parent;
        parent.addFunction(func.getName(), func);
        this.definition = func;
        this.localBindings = new HashMap<>();
        this.criticalMap = new HashMap<>();
        this.preorderQueue = new ArrayDeque<>();
        preorderQueue.add(new BasicBlock());
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
        return preorderQueue.peek();
    }

    public Type getTypeOfDeclaration(String id) {
        for (Declaration decl : definition.concatDecls()) {
            if (decl.getName().equals(id)) {
                return decl.getType();
            }
        }
        throw new RuntimeException("getTypeofDeclaration: shouldn't be here");
    }


    public Deque<BasicBlock> getPreorderQueue() {
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


    /* Register to instruction map */

    public Set<Register> getAllCriticalRegisters() {
        return criticalMap.keySet();
    }

    public void addCriticalRegister(Register result, Instruction inst) {
        criticalMap.put(result, inst);
    }

    public Instruction dropCriticalRegister(Register result) {
        return criticalMap.remove(result);
    }

    public Instruction getCriticalInstruction(Register result) {
        return criticalMap.get(result);
    }

    public void resetCriticalRegisters(Map<Register, Instruction> newMap) {
        criticalMap.clear();
        criticalMap.putAll(newMap);
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

    public void initTransformStructures() {
        for (BasicBlock block : getPreorderQueue()) {
            for (Instruction code : block.getContents()) {
                ((LLVMInstruction) code).setBlock(block);
                addCriticalRegister(code.getResult(), code);
            }
        }
    }

    public void basicTransformations() {
        bubblePhisToTop();
        boolean check = true;
        while (check) {
            check = removeRedundantPhis();
            check |= constantPropAndFold();
            check |= deadCode();
        }
        bubblePhisToTop();
    }

    /**
     * Cleans up any phis with >1 operand from the same location
     * or any operands that match the result
     */
    private boolean removeRedundantPhis() {
        boolean check = false;
        for (BasicBlock block : getPreorderQueue()) {
            Queue<Instruction> code = block.getContents();
            int size = code.size();
            for (int i = 0; i < size; i++) {
                Instruction inst = code.poll();
                if (!(inst instanceof PhiLLVMInstruction)) {
                    code.add(inst);
                    continue;
                }
                PhiLLVMInstruction phi = (PhiLLVMInstruction) inst;
                if (!(phi.isRedundant())) {
                    code.add(inst);
                    continue;
                }
                check = true;
                dropCriticalRegister(phi.getResult());
                substAllSources(phi.getResult(), phi.getSource(0));
            }
        }
        return check;
    }


    /**
     * For all arithmetic/logical expressions
     * if both operands are constant, evaluate the expression and
     * propagate its result throughout the program
     */
    private boolean constantPropAndFold() {
        boolean check = false;
        for (BasicBlock block : getPreorderQueue()) {
            Queue<Instruction> code = block.getContents();
            int size = code.size();
            for (int i = 0; i < size; i++) {
                Instruction inst = code.poll();
                if (!(inst instanceof FoldableInstruction)) {
                    code.add(inst);
                    continue;
                }
                FoldableInstruction fold = (FoldableInstruction) inst;
                Literal constant = fold.fold();
                if (constant == null) {
                    code.add(inst);
                } else {
                    dropCriticalRegister(fold.getResult());
                    substAllSources(fold.getResult(), constant);
                    check = true;
                }
            }
        }
        return check;
    }


    /*
     * removes unreachable/unimportant code and control flow
     */
    private boolean deadCode() {
        boolean check = false;
        Deque<BasicBlock> blockQueue = getPreorderQueue();
        Queue<Instruction> workList = new ArrayDeque<>();
        Map<Register, Instruction> criticalRegisters = new HashMap<>();

        for (BasicBlock block : blockQueue) {
            block.markDead();
            for (Instruction code : block.getContents()) {
                ((LLVMInstruction) code).setBlock(block);
                ((LLVMInstruction) code).markDead();
            }
        }

        // mark the return statement critical and add it to the worklist
        // also mark the epilogue block critical
        BasicBlock epilogue = blockQueue.peekLast();
        assert epilogue != null;
        epilogue.markCritical();

        // mark all instructions that are critical by default
        // add them to the worklist
        for (BasicBlock block : blockQueue) {
            for (Instruction inst : block.getContents()) {
                ((LLVMInstruction) inst).markDead();
                if (inst instanceof CriticalInstruction) {
                    ((LLVMInstruction) inst).markCritical();
                    block.markCritical();
                    workList.add(inst);
                }
            }
        }

        // for each instruction in the worklist
        while (!workList.isEmpty()) {
            Instruction inst = workList.poll();
            ((LLVMInstruction) inst).getBlock().markCritical();
            // mark all of its sources and their blocks critical
            for (Source source : inst.getSources()) {
                if (source instanceof Register) {
                    Register sourceReg = (Register) source;
                    Instruction sourceDefn = getCriticalInstruction(sourceReg);
                    if (sourceDefn != null) {
                        ((LLVMInstruction)sourceDefn).markCritical();
                        dropCriticalRegister(sourceReg);
                        criticalRegisters.put(sourceReg, sourceDefn);
                        workList.add(sourceDefn);
                    }
                }
            }

            if (inst instanceof PhiLLVMInstruction) {
                for (int i = 0; i < inst.getSources().size(); i++) {
                    Label label  = ((PhiLLVMInstruction) inst).getMemberLabel(i);
                    BasicBlock block = null;
                    for (BasicBlock temp : getPreorderQueue()) {
                        if (temp.getLabel().equals(label)) {
                            block = temp;
                            break;
                        }
                    }
                    Instruction branch = block.getContents().peekLast();
                    if (!(branch instanceof JumpInstruction))
                        throw new RuntimeException("Dead Code: Basic block doesn't end in a branch");
                    if (((LLVMInstruction)branch).getDeathMark()) {
                        ((LLVMInstruction)branch).markCritical();
                        block.markCritical();
                        workList.add(branch);
                    }
                }
            }

            // mark all branches that reach this instruction critical
            BasicBlock instBlock = ((LLVMInstruction)inst).getBlock();
            List<BasicBlock> frontier = instBlock.computeRDF();
            for (BasicBlock dominator : frontier) {
                Instruction branch = dominator.getContents().peekLast();
                if (!(branch instanceof JumpInstruction))
                    throw new RuntimeException("Dead Code: Basic block doesn't end in a branch");
                if (((LLVMInstruction)branch).getDeathMark()) {
                    ((LLVMInstruction)branch).markCritical();
                    dominator.markCritical();
                    workList.add(branch);
                }
            }
        }

        // gotta sweep sweep sweep (since this is mark and sweep)
        for (BasicBlock block : blockQueue) {
            int size = block.getContents().size();
            for (int i = 0; i < size; i++) {
                Instruction inst = block.getContents().poll();
                assert inst != null;
                // if the instruction is dead
                if (((LLVMInstruction) inst).getDeathMark()) {
                    // if the instruction is a branch
                    if (inst instanceof JumpInstruction) {
                        // replace the branch with a unconditional jump to the nearest post-dominator
                        BasicBlock postDom = block.computeNearestPostDominator();
                        block.removeChildren();
                        block.addChild(postDom);
                        block.addCode(new UnconditionalBranchLLVMInstruction(postDom.getLabel()));
                    } else {
                        check = true;
                    }
                } else {
                    // if critical, leave it alone (duh)
                    block.addCode(inst);
                }
            }
        }

        // get rid of dead blocks
        int size = blockQueue.size();
        for (int i = 0; i < size; i++) {
            BasicBlock block = blockQueue.poll();
            assert block != null;
            if (block.getParents().size() > 0 || !block.isDead()) {
                blockQueue.add(block);
            } else {
                block.removeChildren();
            }
        }

        // ensure the map of critical register to instructions is up to date
        resetCriticalRegisters(criticalRegisters);

        // clean up redundant branches;
        cleanCFG();
        return check;
    }


    private void cleanCFG() {
        boolean check = true;
        while (check) {
            Queue<BasicBlock> postOrderQueue = getBody().computePostorder();
            check = branchReduction(postOrderQueue);
        }
    }

    private boolean branchReduction(Queue<BasicBlock> postOrderQueue) {
        boolean check = false;
        for (BasicBlock block : postOrderQueue) {
            Instruction last = block.getContents().peekLast();
            if (last instanceof ConditionalBranchLLVMInstruction) {
                ConditionalBranchLLVMInstruction cond = (ConditionalBranchLLVMInstruction) last;
                if (cond.getTrueStub().equals(cond.getFalseStub())) {
                    block.getContents().removeLast();
                    block.addCode(new UnconditionalBranchLLVMInstruction(cond.getTrueStub()));
                }
            }
            if (last instanceof UnconditionalBranchLLVMInstruction) {
                Label destinationLabel = ((UnconditionalBranchLLVMInstruction) last).getStub();
                BasicBlock destination = null;
                for (BasicBlock item : postOrderQueue) {
                    if (item.getLabel().equals(destinationLabel)) {
                        destination = item;
                        break;
                    }
                }
                // if the block is empty
                if (block.getContents().size() == 1) {
                    // get all phi instructions in the destination block
                    List<PhiLLVMInstruction> phis = new ArrayList<>();
                    for (Instruction inst : destination.getContents()) {
                        if (inst instanceof PhiLLVMInstruction)
                            phis.add((PhiLLVMInstruction) inst);
                    }

                    // check if any of the phi rely on parents of the current block
                    // if they do, we can't reduce
                    boolean cantReduce = false;
                    for (PhiLLVMInstruction phi : phis) {
                        for (Label label : phi.getMemberLabels()) {
                            for (BasicBlock parent : block.getParents()) {
                                if (parent.getLabel().equals(label)) {
                                    cantReduce = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (cantReduce)
                        continue;

                    // update the phis to contain references to the parents rather than the block
                    for (PhiLLVMInstruction phi : phis) {
                        int size = phi.getSources().size();
                        List<Source> sources = new ArrayList<>();
                        List<Label> labels = new ArrayList<>();
                        for (int i = 0; i < size; i++) {
                            if (phi.getMemberLabel(i).equals(block.getLabel())) {
                                for (BasicBlock parent : block.getParents()) {
                                    sources.add(phi.getSource(i));
                                    labels.add(parent.getLabel());
                                }
                            } else {
                                sources.add(phi.getSource(i));
                                labels.add(phi.getMemberLabel(i));
                            }
                        }
                        phi.setSources(sources);
                        phi.setMemberLabels(labels);
                    }

                    // replace all references to the block with references to its destination
                    substAllLabels(block.getLabel(), destination.getLabel());

                    for (BasicBlock parent : block.getParents()) {
                        parent.getChildren().remove(block);
                        parent.addChild(destination);
                    }
                    block.getChildren().clear();
                    block.getParents().clear();
                } else if (destination.getParents().size() == 1) {
                    // copy all of the blocks contents into destination
                    block.getContents().removeLast();
                    block.getContents().addAll(destination.getContents());
                    destination.getContents().clear();
                    destination.getContents().addAll(block.getContents());

                    // replace all references to the block with references to its destination
                    substAllLabels(block.getLabel(), destination.getLabel());
                    // update parent's child list to point to destination rather than block
                    for (BasicBlock parent : block.getParents()) {
                        parent.getChildren().remove(block);
                        parent.addChild(destination);
                    }
                    block.getParents().clear();
                    block.getChildren().clear();
                } else if (destination.getContents().size() == 1
                        && destination.getContents().peekLast() instanceof ConditionalBranchLLVMInstruction) {
                    ConditionalBranchLLVMInstruction cond = (ConditionalBranchLLVMInstruction) destination.getContents().getLast();
                    block.getContents().removeLast();
                    block.getContents().add(cond);
                    block.getChildren().remove(destination);

                    for (BasicBlock child : destination.getChildren()) {
                        child.getParents().remove(destination);
                        block.addChild(child);
                    }
                    destination.getChildren().clear();
                    destination.getParents().clear();
                }
            }
        }

        Queue<BasicBlock> preOrderQueue = getPreorderQueue();
        int size = preOrderQueue.size();
        if (size == 1)
            return false;

        for (int i = 0; i < size; i++) {
            BasicBlock block = preOrderQueue.poll();
            if (!(block.getParents().size() == 0 && block.getChildren().size() == 0)) {
                preOrderQueue.add(block);
            } else {
                check = true;
            }
        }
        return check;
    }


    /**
     * Move all phis in a basic block to the top
     */
    private void bubblePhisToTop() {
        for (BasicBlock block : getPreorderQueue()) {
            Queue<Instruction> code = block.getContents();
            Queue<Instruction> buffer = new ArrayDeque<>();
            int size = code.size();
            for (int i = 0; i < size; i++) {
                Instruction inst = code.poll();
                if (inst instanceof PhiLLVMInstruction) {
                    code.add(inst);
                } else {
                    buffer.add(inst);
                }
            }
            while(!buffer.isEmpty()) {
                code.add(buffer.poll());
            }
        }
    }

    /**
     * reduce unnecessary control flow with predicate instructions
     */
    public void selectTransform() {
        List<BasicBlock> removed = new ArrayList<>();
        for (BasicBlock block : preorderQueue) {
            if (!(block.getContents().size() == 1
                && block.getParents().size() == 1
                && block.getChildren().size() == 1)){
                continue;
            }

            BasicBlock child = block.getChildren().get(0);
            BasicBlock parent = block.getParents().get(0);
            if (!child.getParents().contains(parent)) {
                continue;
            }

            List<PhiLLVMInstruction> phis = new ArrayList<>();
            for (Instruction code : child.getContents()) {
                if (code instanceof PhiLLVMInstruction)
                    phis.add((PhiLLVMInstruction) code);
            }

            // remove the conditional branch

            Instruction branch = parent.getContents().removeLast();
            if (!(branch instanceof ConditionalBranchLLVMInstruction))
                throw new RuntimeException("selectTransform: branch must be a Conditional Branch");

            Source cond = ((ConditionalBranchLLVMInstruction) branch).getSource(0);

            for (PhiLLVMInstruction phi : phis) {
                int parentIndex = phi.getMemberLabels().indexOf(parent.getLabel());
                Source left = phi.getSource(parentIndex);
                phi.getSources().remove(parentIndex);
                phi.getMemberLabels().remove(parentIndex);
                int blockIndex = phi.getMemberLabels().indexOf(block.getLabel());
                Source right = phi.getSource(blockIndex);
                phi.getSources().remove(blockIndex);
                phi.getMemberLabels().remove(blockIndex);

                Register selectResult = Register.genTypedLocalRegister(left.getType().copy(), parent.getLabel());
                SelectLLVMInstruction select = new SelectLLVMInstruction(selectResult, cond, left, right);
                parent.addCode(select);
                phi.addMember(new PhiTuple(selectResult, parent.getLabel()));
            }

            parent.addCode(new UnconditionalBranchLLVMInstruction(child.getLabel()));
            parent.getChildren().remove(block);
            child.getParents().remove(block);
            block.getParents().clear();
            block.getChildren().clear();
            removed.add(block);
        }
        for (BasicBlock block : removed) {
            preorderQueue.remove(block);
        }
    }



    /**
     * Search for occurrences of the source 'original' through the function
     * If found, replace it with the source 'replacement'
     */
    private void substAllSources(Source original, Source replacement) {
        for (BasicBlock block : getPreorderQueue()) {
            Queue<Instruction> code = block.getContents();
            for (Instruction inst : code) {
                inst.substituteSource(original.copy(), replacement.copy());

                // update block bindings
                Map<String, Source> bindings = block.getLocalBindings();
                for (Map.Entry<String, Source> entry : bindings.entrySet()) {
                    if (Objects.equals(original, entry.getValue())) {
                        bindings.put(entry.getKey(), replacement);
                    }
                }

            }
        }
    }

    /**
     * Search for occurrences of the label 'original' through the function
     * If found, replace it with the label 'replacement'
     */
    private void substAllLabels(Label original, Label replacement) {
        for (BasicBlock block : getPreorderQueue()) {
            Queue<Instruction> code = block.getContents();
            for (Instruction inst : code) {
                // this is done so that if result is updated it will have the proper hash
                // mutation is the death of a hashtable
                Instruction dupInst = dropCriticalRegister(inst.getResult());
                inst.substituteLabel(original, replacement);
                if (dupInst != null) {
                    addCriticalRegister(inst.getResult(), inst);
                }
            }
        }
    }


    /*
    Queue<BasicBlock> toArm() {
        Queue<BasicBlock> armQueue = new ArrayDeque<>();
        int allocaSpace = 0;
        for (BasicBlock block : preorderQueue) {
            BasicBlock armBlock = new BasicBlock();
            Label armLabel = new Label(definition.getName() + "." + block.getLabel().getName());
            armBlock.setLabel(armLabel);
            for (Instruction inst : block.getContents()) {
                if (!(inst instanceof LLVMInstruction))
                    throw new RuntimeException("Input should be unaltered list of LLVM code");

                if (inst instanceof AbstractLLVMInstruction) {
                    allocaSpace += 8;
                } else if 
            }
        }
        return null;
    }
     */
}
