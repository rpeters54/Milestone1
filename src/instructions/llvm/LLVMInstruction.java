package instructions.llvm;

import ast.BasicBlock;
import instructions.Instruction;
import instructions.arm.ArmInstruction;

import java.util.List;

public interface LLVMInstruction extends Instruction {
    // critical/dead mark for dead code elim
    void markDead();
    void markCritical();
    boolean getDeathMark();

    // block where instruction was defined
    BasicBlock getBlock();
    void setBlock(BasicBlock block);

    List<ArmInstruction> toArm();

}
