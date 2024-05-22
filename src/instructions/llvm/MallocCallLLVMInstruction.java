package instructions.llvm;

import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;
import instructions.arm.BranchLinkArmInstruction;
import instructions.arm.MovArmInstruction;

import java.util.ArrayList;
import java.util.List;

public class MallocCallLLVMInstruction extends AbstractLLVMInstruction implements CriticalInstruction {

    public MallocCallLLVMInstruction(Register result, Source size) {
        super(result, new ArrayList<>());
        addSource(size);
    }

    private Source size() {
        return super.getSource(0);
    }

    @Override
    public String toString() {
        return String.format("%s = call i8* @malloc(%s %s)",
               getResult(), size().getTypeString(), size());
    }

    @Override
    public List<ArmInstruction> toArm() {
        List<ArmInstruction> instList = new ArrayList<>();
        instList.add(new MovArmInstruction(Register.genArmRegister(0), getSource(0)));
        instList.add(new BranchLinkArmInstruction("malloc", 1));
        instList.add(new MovArmInstruction(getResult(), Register.genArmRegister(0)));
        return instList;
    }
}
