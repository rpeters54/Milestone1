package instructions.llvm;

import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;
import instructions.arm.BranchLinkArmInstruction;
import instructions.arm.MovArmInstruction;

import java.util.ArrayList;
import java.util.List;

public class FreeCallLLVMInstruction extends AbstractLLVMInstruction {

    public FreeCallLLVMInstruction(Source ptr) {
        super(null, new ArrayList<>());
        addSource(ptr);
    }

    @Override
    public String toString() {
        return String.format("call void @free(i8* %s)", getSource(0));
    }


    @Override
    public List<ArmInstruction> toArm() {
        List<ArmInstruction> instList = new ArrayList<>();
        instList.add(new MovArmInstruction(Register.genArmRegister(0), getSource(0)));
        instList.add(new BranchLinkArmInstruction("free", 1));
        return instList;
    }
}
