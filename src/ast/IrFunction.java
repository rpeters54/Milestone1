package ast;

import ast.declarations.Declaration;
import ast.expressions.BinaryExpression;
import ast.types.ArrayAllocType;
import ast.types.IntType;
import ast.types.PointerType;
import ast.types.Type;
import ast.declarations.TypeDeclaration;
import instructions.*;
import instructions.arm.*;
import instructions.llvm.*;


import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class IrFunction {
    public static final int baseSize = 96;
    public static final int cappedSize = 496;

    private final IrProgram parent;
    private final Function definition;
    private final Map<String, Register> localBindings;   //local symbol table
    private final Deque<BasicBlock> preorderQueue;
    private Deque<BasicBlock> armQueue;
    private int stackSize;


    public IrFunction(IrProgram parent, Function func) {
        this.parent = parent;
        parent.addFunction(func.getName(), func);
        this.definition = func;
        this.localBindings = new HashMap<>();
        this.preorderQueue = new ArrayDeque<>();
        preorderQueue.add(new BasicBlock());
        this.armQueue = new ArrayDeque<>();
        this.stackSize = baseSize;
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

    public Deque<BasicBlock> getArmQueue() {
        return armQueue;
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
        writeHeader(writer);
        for (BasicBlock block : preorderQueue) {
            writeContents(writer, block.getLabel(), block.getContents());
        }
        writeEOF(writer);
    }

    public void toArmFile(FileWriter writer) throws IOException {
        writer.write(String.format(".global %s\n", definition.getName()));
        writer.write(String.format("%s:\n",definition.getName()));
        for (BasicBlock block : armQueue) {
            writeContents(writer, block.getLabel(), block.getContents());
        }
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


    public void basicTransformations() {
        bubblePhisToTop();
        boolean check = true;
        while (check) {
            check = removeRedundantPhis();
            check |= constantPropAndFold();
            check |= deadCode();
            //check |= selectTransform();
        }
        bubblePhisToTop();
    }

    /**
     * Cleans up any phis with >1 operand from the same location
     * or any operands that match the result
     */
    private boolean removeRedundantPhis() {
        boolean check = false;
        // for each block in the queue
        for (BasicBlock block : getPreorderQueue()) {
            Queue<Instruction> code = block.getContents();
            int size = code.size();
            // for each instruction in the block
            for (int i = 0; i < size; i++) {
                // if the instruction is not a phi, keep it
                Instruction inst = code.poll();
                if (!(inst instanceof PhiLLVMInstruction)) {
                    code.add(inst);
                    continue;
                }
                // if the instruction is a phi, but it is not redundant, keep it
                PhiLLVMInstruction phi = (PhiLLVMInstruction) inst;
                if (!(phi.isRedundant())) {
                    code.add(inst);
                    continue;
                }
                check = true;
//                // remove the phi's result from the set of critical registers
//                dropCriticalRegister(phi.getResult());
                // redundant phis will always have a single source (at least in my implementation)
                // replace the phi with its only source
                substAllLLVMSources(phi.getResult(), phi.getSource(0));
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
        // for each block in the queue
        for (BasicBlock block : getPreorderQueue()) {
            Queue<Instruction> code = block.getContents();
            int size = code.size();
            // for each instruction in the block
            for (int i = 0; i < size; i++) {
                Instruction inst = code.poll();
                // if the instruction is not foldable, keep it
                if (!(inst instanceof FoldableInstruction)) {
                    code.add(inst);
                    continue;
                }
                // if the instruction can be folded
                FoldableInstruction fold = (FoldableInstruction) inst;
                Literal constant = fold.fold();
                // if it did not fold to a literal, keep the original instruction
                if (constant == null) {
                    code.add(inst);
                // if it did fold to a literal, remove the binary expression
                // and substitute the old result with the literal
                } else {
//                    dropCriticalRegister(fold.getResult());
                    substAllLLVMSources(fold.getResult(), constant);
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

        // mark each block and instruction dead by default
        // also initialize map from registers to instructions
        for (BasicBlock block : blockQueue) {
            block.markDead();
            for (Instruction code : block.getContents()) {
                criticalRegisters.put(code.getResult(), code);
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
                    Instruction sourceDefn = criticalRegisters.get(sourceReg);
                    if (sourceDefn != null) {
                        ((LLVMInstruction)sourceDefn).markCritical();
                        criticalRegisters.remove(sourceReg);
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

        // clean up redundant branches;
        cleanCFG();
        return check;
    }


    /**
     * This cleans up all the empty blocks and unconditional branches left
     * behind by the mark and sweep
     */
    private void cleanCFG() {
        boolean check = true;
        while (check) {
            Queue<BasicBlock> postOrderQueue = getBody().computePostorder();
            check = branchReduction(postOrderQueue);
        }
    }

    /**
     * Given a queue basicblocks in postorder, remove all unnecessary blocks
     * @param postOrderQueue
     * @return
     */
    private boolean branchReduction(Queue<BasicBlock> postOrderQueue) {
        boolean check = false;
        // for each block in the queue
        for (BasicBlock block : postOrderQueue) {
            Instruction last = block.getContents().peekLast();
            // if the last instruction is a conditional branch
            if (last instanceof ConditionalBranchLLVMInstruction) {
                ConditionalBranchLLVMInstruction cond = (ConditionalBranchLLVMInstruction) last;
                // if both branches are to the same location, replace the branch with an unconditional jump
                if (cond.getTrueStub().equals(cond.getFalseStub())) {
                    block.getContents().removeLast();
                    block.addCode(new UnconditionalBranchLLVMInstruction(cond.getTrueStub()));
                }
            }
            // if the last instruction is an unconditional branch
            if (last instanceof UnconditionalBranchLLVMInstruction) {
                // get the block 'destination' that the unconditional branch label refers to
                // (I know there are more efficient ways to do this, but this worked without changing all my old code)
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
                    boolean reduceable = true;
                    for (PhiLLVMInstruction phi : phis) {
                        for (Label label : phi.getMemberLabels()) {
                            for (BasicBlock parent : block.getParents()) {
                                if (parent.getLabel().equals(label)) {
                                    reduceable = false;
                                    break;
                                }
                            }
                        }
                    }

                    // if we can reduce it
                    if (reduceable) {
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
                        substAllLLVMLabels(block.getLabel(), destination.getLabel());

                        // transform edges to properly connect the blocks
                        destination.getParents().remove(block);
                        for (BasicBlock parent : block.getParents()) {
                            parent.getChildren().remove(block);
                            parent.addChild(destination);
                        }
                        block.getChildren().clear();
                        block.getParents().clear();
                    }
                } else if (destination.getParents().size() == 1) {
                    // copy all of the blocks contents into destination
                    block.getContents().removeLast();
                    block.getContents().addAll(destination.getContents());
                    destination.getContents().clear();
                    destination.getContents().addAll(block.getContents());
                    destination.getParents().remove(block);

                    // replace all references to the block with references to its destination
                    substAllLLVMLabels(block.getLabel(), destination.getLabel());

                    // update parent's child list to point to destination rather than block
                    for (BasicBlock parent : block.getParents()) {
                        parent.getChildren().remove(block);
                        parent.addChild(destination);
                    }
                    block.getParents().clear();
                    block.getChildren().clear();
                // if the destination block is just a conditional branch, hoist the branch
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
        // move prologue to the back so it is ignored
        BasicBlock prologue = preOrderQueue.poll();
        preOrderQueue.add(prologue);
        for (int i = 1; i < size; i++) {
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
    public boolean selectTransform() {
        boolean check = false;
        List<BasicBlock> removed = new ArrayList<>();
        for (BasicBlock block : preorderQueue) {
            // if a block does not:
            // 1. contain only a branch
            // 2. have only one parent and child
            // ignore it
            if (!(block.getContents().size() == 1
                && block.getParents().size() == 1
                && block.getChildren().size() == 1)){
                continue;
            }

            // if a block is contained within a loop, ignore it
            BasicBlock child = block.getChildren().get(0);
            BasicBlock parent = block.getParents().get(0);
            if (!child.getParents().contains(parent)) {
                continue;
            }

            // track all phis within the child
            List<PhiLLVMInstruction> phis = new ArrayList<>();
            for (Instruction code : child.getContents()) {
                if (code instanceof PhiLLVMInstruction)
                    phis.add((PhiLLVMInstruction) code);
            }

            // remove the conditional branch
            Instruction branch = parent.getContents().removeLast();
            if (!(branch instanceof ConditionalBranchLLVMInstruction))
                throw new RuntimeException("selectTransform: branch must be a Conditional Branch");


            // for each phi
            Source cond = ((ConditionalBranchLLVMInstruction) branch).getSource(0);
            for (PhiLLVMInstruction phi : phis) {
                // replace the references to the parent and block with the result of a select instruction
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

            // fix the branches
            parent.addCode(new UnconditionalBranchLLVMInstruction(child.getLabel()));
            parent.getChildren().remove(block);
            child.getParents().remove(block);
            block.getParents().clear();
            block.getChildren().clear();
            removed.add(block);
        }
        // remove all marked blocks
        for (BasicBlock block : removed) {
            check = true;
            preorderQueue.remove(block);
        }
        return check;
    }



    /**
     * Search for occurrences of the source 'original' through the function
     * If found, replace it with the source 'replacement'
     */
    private void substAllLLVMSources(Source original, Source replacement) {
        for (BasicBlock block : getPreorderQueue()) {
            for (Instruction inst : block.getContents()) {
                if (!(inst instanceof LLVMInstruction)) {
                    throw new RuntimeException("substAllLLVMSources: instruction not an LLVM instruction");
                }
                inst.substituteSource(original.copy(), replacement.copy());
            }
        }
    }

    private void substAllArmSources(Source original, Source replacement) {
        for (BasicBlock block : getArmQueue()) {
            for (Instruction inst : block.getContents()) {
                if (!(inst instanceof ArmInstruction)) {
                    throw new RuntimeException("substAllArmSources: instruction not an Arm instruction");
                }
                inst.substituteSource(original.copy(), replacement.copy());
            }
        }
    }

    /**
     * Search for occurrences of the label 'original' through the function
     * If found, replace it with the label 'replacement'
     */
    private void substAllLLVMLabels(Label original, Label replacement) {
        for (BasicBlock block : getPreorderQueue()) {
            for (Instruction inst : block.getContents()) {
                if (!(inst instanceof LLVMInstruction)) {
                    throw new RuntimeException("substAllLLVMLabels: instruction not an LLVM instruction");
                }
                inst.substituteLabel(original, replacement);
            }
        }
    }

    private void substAllArmLabels(Label original, Label replacement) {
        for (BasicBlock block : getArmQueue()) {
            for (Instruction inst : block.getContents()) {
                if (!(inst instanceof ArmInstruction)) {
                    throw new RuntimeException("substAllArmLabels: instruction not an LLVM instruction");
                }
                inst.substituteLabel(original, replacement);
            }
        }
    }


    private void copyBlocks(Deque<BasicBlock> base, Deque<BasicBlock> copy) {
        // generate all basic blocks
        copy.clear();
        for (BasicBlock block : base) {
            BasicBlock armBlock = new BasicBlock();
            Label armLabel = new Label(definition.getName() + "_" + block.getLabel().getName());
            armBlock.setLabel(armLabel);
            copy.add(armBlock);
        }
    }


    private void copyEdges(Deque<BasicBlock> base, Deque<BasicBlock> copy) {
        int size = base.size();
        for (int i = 0; i < size; i++) {
            BasicBlock llvm = base.peek();
            BasicBlock arm = copy.peek();
            for (int j = 0; j < size; j++) {
                BasicBlock llvmTemp = base.peek();
                BasicBlock armTemp = copy.peek();
                if (llvm.getChildren().contains(llvmTemp)) {
                    arm.addChild(armTemp);
                }
                base.poll();
                copy.poll();
                base.add(llvmTemp);
                copy.add(armTemp);
            }
            base.poll();
            copy.poll();
            base.add(llvm);
            copy.add(arm);
        }
    }

    private void addArgMoves(BasicBlock prologue) {
        for (int i = 0; i < definition.getParams().size(); i++) {
            String regname = "r"+i;
            Register argreg = null;
            boolean found = false;
            for (BasicBlock block : preorderQueue) {
                if (found)
                    break;
                for (Instruction inst : block.getContents()) {
                    if (found)
                        break;
                    for (Register result : inst.getResults()) {
                        if (result != null && regname.equals(result.toString())) {
                            argreg = result;
                            found = true;
                            break;
                        }
                    }
                    for (Source source : inst.getSources()) {
                        if (source != null && source instanceof Register && regname.equals(source.toString())) {
                            argreg = (Register) source;
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (found)
                prologue.addCode(new MovArmInstruction(argreg, Register.genArmRegister(i)));
        }
    }


    private void translateInstructions(Deque<BasicBlock> llvmBlocks, Deque<BasicBlock> armBlocks) {
        for (int i = 0; i < llvmBlocks.size(); i++) {
            BasicBlock llvm = llvmBlocks.poll();
            BasicBlock arm = armBlocks.poll();
            for (Instruction inst : llvm.getContents()) {
                if (!(inst instanceof LLVMInstruction))
                    throw new RuntimeException("IrFunction::toArm: Input should be unaltered list of LLVM code");
                List<Instruction> armInst = ((LLVMInstruction) inst).toArm();
                for (Instruction newInst : armInst) {
                    for (int j = 0; j < newInst.getSources().size(); j++) {
                        Source source = newInst.getSources().get(j);
                        if (source instanceof Literal) {
                            newInst.getSources().set(j, ((Literal)source).toArm());
                        }
                    }
                }
                arm.getContents().addAll(armInst);

            }
            llvmBlocks.add(llvm);
            armBlocks.add(arm);
        }
    }


    private void translateLabels(Deque<BasicBlock> oldQueue, Deque<BasicBlock> newQueue) {
        // keep track of old and new labels
        List<Label> oldLabels = new ArrayList<>();
        List<Label> newLabels = new ArrayList<>();
        for (int i = 0; i < oldQueue.size(); i++) {
            BasicBlock llvm = oldQueue.poll();
            BasicBlock arm = newQueue.poll();
            oldLabels.add(llvm.getLabel());
            newLabels.add(arm.getLabel());
            oldQueue.add(llvm);
            newQueue.add(arm);
        }

        for (BasicBlock newBlock : newQueue) {
            for (Instruction inst : newBlock.getContents()) {
                for (int k = 0; k < oldLabels.size(); k++) {
                    inst.substituteLabel(oldLabels.get(k), newLabels.get(k));
                }
            }
        }
    }


    private void addIntermediateBlock(BasicBlock arm, BasicBlock llvm,
                                      List<PhiLLVMInstruction> phis, Deque<BasicBlock> blockQueue) {
        BasicBlock paradox = arm.paradox();
        if (paradox != null) {
            // generate a new intermediate block between the block and its parent
            BasicBlock intermediate = new BasicBlock();
            Label intermediateLabel = new Label("inter_" + arm.getLabel().getName());
            intermediate.setLabel(intermediateLabel);
            intermediate.addCode(new UnconditionalBranchArmInstruction(arm.getLabel()));
            // update phi labels
            for (PhiLLVMInstruction phi : phis) {
                Label oldLabel = paradox.getLabel();
                int index = phi.getIndexByLabel(oldLabel);
                phi.setMemberLabel(index, intermediateLabel);
            }
            // break the edge between the block and its parent
            arm.getParents().remove(paradox);
            paradox.getChildren().remove(arm);
            Instruction unconditional = paradox.getContents().removeLast();
            if (!(unconditional instanceof UnconditionalBranchArmInstruction))
                throw new RuntimeException("block should end with unconditional");
            Instruction conditional = paradox.getContents().removeLast();
            if (!(conditional instanceof ConditionalBranchArmInstruction))
                throw new RuntimeException("second to last instruction should be conditional");
            UnconditionalBranchArmInstruction uncond = (UnconditionalBranchArmInstruction) unconditional;
            ConditionalBranchArmInstruction cond = (ConditionalBranchArmInstruction) conditional;
            if (uncond.getDestination() == arm.getLabel())
                uncond.setDestination(intermediateLabel);
            if (cond.getDestination() == arm.getLabel())
                cond.setDestination(intermediateLabel);
            paradox.addCode(cond);
            paradox.addCode(uncond);
            // insert the intermediate block
            paradox.addChild(intermediate);
            intermediate.addChild(arm);
            blockQueue.add(intermediate);
        }

        // add all the moves
        for (BasicBlock predecessor : arm.getParents()) {
            List<MovArmInstruction> movs = new ArrayList<>();
            for (PhiLLVMInstruction phi : phis) {
                int index = phi.getIndexByLabel(predecessor.getLabel());
                Source source = phi.getSource(index);
                movs.add(new MovArmInstruction(phi.getResult(), source));
            }
            boolean swap = true;
            while (swap) {
                swap = false;
                for (int i = 0; i < movs.size(); i++) {
                    for (int j = i + 1; j < movs.size(); j++) {
                        MovArmInstruction early = movs.get(i);
                        MovArmInstruction later = movs.get(j);
                        if (later.getSources().contains(early.getResult())) {
                            swap = true;
                            movs.set(i, later);
                            movs.set(j, early);
                            break;
                        }
                    }
                }
            }
            // otherwise just insert the move instruction before the branch
            Stack<Instruction> branchStack = new Stack<>();
            while (predecessor.getContents().peekLast() instanceof UnconditionalBranchArmInstruction
                    || predecessor.getContents().peekLast() instanceof ConditionalBranchArmInstruction) {
                branchStack.push(predecessor.getContents().removeLast());
            }
            predecessor.getContents().addAll(movs);
            while(!branchStack.isEmpty()) {
                predecessor.addCode(branchStack.pop());
            }
        }
    }


    void toArm() {
        // generate all basic blocks
        copyBlocks(preorderQueue, armQueue);
        // generate all edges
        copyEdges(preorderQueue, armQueue);

        // add explicit moves from argument registers
        BasicBlock prologue = armQueue.peek();
        addArgMoves(prologue);
        // add stack builder
        prologue.getContents().addFirst(new BuildStackArmInstruction());

        // translate all instructions
        translateInstructions(preorderQueue, armQueue);

        //add stack destructor
        BasicBlock last = armQueue.peekLast();
        Instruction returnInst = last.getContents().removeLast();
        if (!(returnInst instanceof ReturnArmInstruction))
            throw new RuntimeException("IrFunction::toArm: Last Instruction should be a Arm Return instruction");
        last.addCode(new DestroyStackArmInstruction());
        last.addCode(returnInst);


        translateLabels(preorderQueue, armQueue);

        List<Register> oldAllocas = new ArrayList<>();
        List<Register> newAllocas = new ArrayList<>();
        // handle allocas and phis
        for (int i = 0; i < preorderQueue.size(); i++) {
            BasicBlock llvm = preorderQueue.peek();
            BasicBlock arm = armQueue.peek();

            List<PhiLLVMInstruction> phis = new ArrayList<>();
            int contentSize = arm.getContents().size();
            for (int j = 0; j < contentSize; j++) {
                Instruction inst = arm.getContents().poll();
                /* replace alloca with additional stack space */
                /* replace register referenced by alloca with pointer arithmetic*/
                if (inst instanceof AllocaLLVMInstruction) {
                    Register allocaLocation = Register.genLocalRegister(arm.getLabel());
                    // create a register that defines a pointer to stack
                    arm.addCode(new BinaryArmInstruction(allocaLocation, BinaryExpression.Operator.PLUS,
                            Register.genStackPointer(), new Literal(new IntType(), Integer.toString(stackSize), arm.getLabel())));
                    Type type = inst.getResult().getType();
                    if (type instanceof PointerType && ((PointerType) type).getBaseType() instanceof ArrayAllocType) {
                        stackSize += 8*Integer.parseInt(((ArrayAllocType) ((PointerType) type).getBaseType()).getSize());
                    } else {
                        stackSize += 8;
                    }
                    oldAllocas.add(inst.getResult());
                    newAllocas.add(allocaLocation);
                } else if (inst instanceof PhiLLVMInstruction) {
                    phis.add((PhiLLVMInstruction) inst);
                } else {
                    arm.getContents().add(inst);
                }
            }
            if (phis.size() > 0) {
                addIntermediateBlock(arm, llvm, phis, armQueue);
            }

            preorderQueue.poll();
            armQueue.poll();
            preorderQueue.add(llvm);
            armQueue.add(arm);
        }
        for (int j = 0; j < oldAllocas.size(); j++) {
            substAllArmSources(oldAllocas.get(j), newAllocas.get(j));
        }

        loadLargeImmediates(armQueue);
        updateStackSize(armQueue, stackSize);
    }

    private void loadLargeImmediates(Deque<BasicBlock> blockQueue) {
        for (BasicBlock block : blockQueue) {
            int size = block.getContents().size();
            for (int i = 0; i < size; i++) {
                Instruction inst = block.getContents().poll();
                for (int j = 0; j < inst.getSources().size(); j++) {
                    Source source = inst.getSources().get(j);
                    if (source instanceof Literal) {
                        long value = Long.parseLong(source.toString());
                        if (value >= 2048 || value <= -2048) {
                            Register temp = Register.genLocalRegister(block.getLabel());
                            block.addCode(new LoadLabelArmInstruction(temp, source.toString()));
                            inst.getSources().set(j, temp);
                        }
                    }
                }
                block.addCode(inst);
            }
        }

    }

    private void updateStackSize(Deque<BasicBlock> newQueue, int space) {
        Instruction first = newQueue.peek().getContents().peek();
        Instruction returnInst = newQueue.peekLast().getContents().removeLast();
        Instruction last = newQueue.peekLast().getContents().peekLast();
        newQueue.peekLast().getContents().add(returnInst);
        if (!(first instanceof BuildStackArmInstruction) || !(last instanceof DestroyStackArmInstruction))
            throw new RuntimeException("updateStackSize: first and second to last " +
                    "instructions should be stack setup and teardown");
        BuildStackArmInstruction build = (BuildStackArmInstruction) first;
        DestroyStackArmInstruction destroy = (DestroyStackArmInstruction) last;
        // make sure the stack is 16 byte aligned
        space += space % 16;
        build.setExtraSpace(space);
        destroy.setExtraSpace(space);
    }



    public void registerAllocation() {
        if (armQueue.size() == 0)
            throw new RuntimeException("registerAllocation: must call toArm() before attempting to register allocate");
        Map<BasicBlock, Set<Register>> livevalues = liveRanges(armQueue);
        Graph<Register> interferenceGraph = buildInterferenceGraph(livevalues, armQueue);
        Map<Register, Register> colorMap = colorGraph(interferenceGraph);
        Map<Register, Integer> spillAddresses = allocateSpillage(colorMap);
        updateStackSize(armQueue, stackSize);
        updateRegisters(colorMap, spillAddresses);
    }

    public Map<BasicBlock, Set<Register>> liveRanges(Deque<BasicBlock> blockQueue) {
        boolean changed = true;
        Map<BasicBlock, Set<Register>> liveOutMap = new HashMap<>();
        while (changed) {
            changed = false;
            int size = blockQueue.size();
            for (int i = 0; i < size; i++) {
                BasicBlock current = blockQueue.removeLast();
                Set<Register> currentSet = liveOutMap.get(current);
                Set<Register> newSet = current.computeLiveOut(liveOutMap);
                if (!newSet.equals(currentSet)) {
                    liveOutMap.put(current, newSet);
                    changed = true;
                }
                blockQueue.addFirst(current);
            }
        }
        return liveOutMap;
    }

    public Graph<Register> buildInterferenceGraph(Map<BasicBlock, Set<Register>> livevalues, Queue<BasicBlock> blockQueue) {
        Graph<Register> interferenceGraph = new Graph<>();
        for (BasicBlock block : blockQueue) {
            Set<Register> liveset = livevalues.get(block);
            int size = block.getContents().size();
            for (int i = 0; i < size; i++) {
                Instruction inst = block.getContents().removeLast();
                List<Register> results = inst.getResults();
                List<Register> sources = inst.getSources().stream()
                        .filter(item -> item instanceof Register)
                        .map(item -> (Register) item)
                        .collect(Collectors.toList());
                // add an interference edge between all live values and the result
                for (Register result : results) {
                    interferenceGraph.addNode(result);
                    for (Register liveReg : liveset) {
                        interferenceGraph.addNode(liveReg);
                        if (!liveReg.equals(result))
                            interferenceGraph.addEdge(result, liveReg);
                    }
                }
                liveset.removeAll(results);
                liveset.addAll(sources);
                block.getContents().addFirst(inst);
            }
        }
        return interferenceGraph;
    }

    public Map<Register, Register> colorGraph(Graph<Register> interferenceGraph) {
        // generate a List of all unreserved registers
        // Note: x19 and x20 are reserved for spillover
        Set<Register> colors = new HashSet<>(Register.genArmRegisterList(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 9,
                10, 11, 12, 13, 14, 15, 21, 22, 23, 24, 25, 26, 27, 28)));
        Map<Register, List<Register>> archivedEdges = new HashMap<>();
        Map<Register, Register> coloring = new HashMap<>();
        // mark all arm registers as their own color
        for (Register color : colors) {
            coloring.put(color, color);
        }
        coloring.put(Register.genStackPointer(), Register.genStackPointer());
        Stack<Register> coloringStack = new Stack<>();

        // this boolean is used to allow the arm registers to remain in the graph
        boolean hasLLVM = true;
        while (hasLLVM && !interferenceGraph.isEmpty()) {
            Register next = null;
            hasLLVM = false;
            // try to select an unconstrained node
            for (Register node : interferenceGraph.getNodeSet()) {
                if (node.isArm()) {
                    continue;
                }
                hasLLVM = true;
                if (interferenceGraph.degree(node) <= colors.size()) {
                    next = node;
                    break;
                }
            }
            // if no such node is found, select a constrained node
            if (next == null) {
                for (Register node : interferenceGraph.getNodeSet()) {
                    if (node.isArm()) {
                        continue;
                    }
                    hasLLVM = true;
                    next = node;
                }
            }
            // if a node has been found, add it to the stack
            if (next != null) {
                List<Register> edgeList = interferenceGraph.removeNode(next);
                archivedEdges.put(next, edgeList);
                coloringStack.push(next);
            }
        }

        while(!coloringStack.isEmpty()) {
            Register next = coloringStack.pop();
            List<Register> edgeList = archivedEdges.get(next);
            interferenceGraph.addNode(next);
            interferenceGraph.addEdges(next, edgeList);

            // convert the edgelist into a set of taken colors
            Set<Register> takenColors = edgeList.stream().map(coloring::get).collect(Collectors.toSet());
            // generate a set of available colors, while is the base set of colors with the taken set removed
            Set<Register> availableColors = new HashSet<>(colors);
            availableColors.removeAll(takenColors);

            // if there exists a color that can be used
            if (availableColors.stream().findFirst().isPresent()) {
                // apply that color to the register
                Register color = availableColors.stream().findFirst().get();
                coloring.put(next, color);
            } else {
                // place a null to mark the register as spillover
                coloring.put(next, null);
            }
        }

        return coloring;
    }

    public Map<Register, Integer> allocateSpillage(Map<Register, Register> coloring) {
        Map<Register, Integer> offsetMap = new HashMap<>();
        List<Register> discardedKeys = new ArrayList<>();
        for (Register key : coloring.keySet()) {
            if (coloring.get(key) == null) {
                discardedKeys.add(key);
            }
        }
        for (Register key : discardedKeys) {
            coloring.remove(key);
            stackSize += 8;
            offsetMap.put(key, stackSize);
        }
        return offsetMap;
    }

    public void updateRegisters(Map<Register, Register> coloring, Map<Register, Integer> spillageAddresses) {
        List<Register> spillRegs = Register.genArmRegisterList(Arrays.asList(19, 20));
        // for each block
        for (BasicBlock block : armQueue) {
            // for each instruction
            int size = block.getContents().size();
            for (int i = 0; i < size; i++) {
                Instruction inst = block.getContents().poll();
                boolean spilled = false;
                // for each source
                for (int j = 0; j < inst.getSources().size(); j++) {
                    Source source = inst.getSources().get(j);
                    // if the source is not a register, ignore it
                    // ignore globals and struct members (they are stored by default)
                    if (!(source instanceof Register) || ((Register) source).isGlobal())
                        continue;
                    Register sourceReg = (Register) source;
                    // if the source was colored, update it to the arm register
                    if (coloring.get(sourceReg) != null) {
                        inst.getSources().set(j, coloring.get(sourceReg));
                    // if the source was not colored, add the spillreg and load
                    } else if (spillageAddresses.get(sourceReg) != null) {
                        Register spillReg = spilled ? spillRegs.get(1) : spillRegs.get(0);
                        inst.getSources().set(j, spillReg);
                        block.addCode(new LoadOffsetArmInstruction(spillReg, spillageAddresses.get(sourceReg)));
                        spilled = true;
                    } else {
                        throw new RuntimeException("updateRegisters: source Register not contained in either map");
                    }
                }
                // add the original instruction back into the basic block to maintain proper ordering
                block.addCode(inst);
                // for each result
                for (int j = 0; j < inst.getResults().size(); j++) {
                    Register result = inst.getResults().get(j);
                    // ignore globals (they are stored by default)
                    if (result.isGlobal())
                        continue;
                    // if the result was colored, update it to the arm register
                    if (coloring.get(result) != null) {
                        inst.getResults().set(j, coloring.get(result));
                    // if the result was spilled, add a store after the instruction
                    } else if (spillageAddresses.get(result) != null) {
                        Register spillReg = spillRegs.get(0);
                        inst.getResults().set(j, spillReg);
                        block.addCode(new StoreOffsetArmInstruction(spillReg, spillageAddresses.get(result)));
                    } else {
                        throw new RuntimeException("updateRegisters: result Register not contained in either map");
                    }
                }
            }
        }
    }
}
