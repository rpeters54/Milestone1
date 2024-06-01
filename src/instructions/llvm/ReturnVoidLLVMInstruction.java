package instructions.llvm;

import instructions.Instruction;
import instructions.arm.ArmInstruction;
import instructions.arm.ReturnArmInstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReturnVoidLLVMInstruction extends AbstractLLVMInstruction implements JumpInstruction, CriticalInstruction {

    public ReturnVoidLLVMInstruction() {
        super(null, new ArrayList<>());
    }

    @Override
    public String toString() {
        return "ret void";
    }

    @Override
    public List<Instruction> toArm() {
        return Collections.singletonList(new ReturnArmInstruction());
    }
}
