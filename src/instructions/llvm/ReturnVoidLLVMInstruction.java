package instructions.llvm;

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
    public List<ArmInstruction> toArm() {
        return Collections.singletonList(new ReturnArmInstruction());
    }
}
