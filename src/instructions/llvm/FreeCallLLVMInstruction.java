package instructions.llvm;

import instructions.Instruction;
import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;
import instructions.arm.BranchLinkArmInstruction;
import instructions.arm.MovArmInstruction;

import java.util.ArrayList;
import java.util.List;

public class FreeCallLLVMInstruction extends AbstractLLVMInstruction implements CriticalInstruction {

    public FreeCallLLVMInstruction(Source ptr) {
        super(null, new ArrayList<>());
        addSource(ptr);
    }

    @Override
    public String toString() {
        return String.format("call void @free(i8* %s)", getSource(0).llvmString());
    }


    @Override
    public List<Instruction> toArm() {
        List<Instruction> instList = new ArrayList<>();
        instList.add(new MovArmInstruction(Register.genArmRegister(0), getSource(0)));
        instList.add(new BranchLinkArmInstruction("free", 1));
        return instList;
    }
}
