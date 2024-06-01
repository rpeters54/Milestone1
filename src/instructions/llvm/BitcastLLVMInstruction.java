package instructions.llvm;

import instructions.Instruction;
import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;
import instructions.arm.MovArmInstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BitcastLLVMInstruction extends AbstractLLVMInstruction {


    public BitcastLLVMInstruction(Register result, Source uncasted) {
        super(result, new ArrayList<>());
        addSource(uncasted);
    }

    @Override
    public String toString() {
        return String.format("%s = bitcast %s %s to %s",
                getResult().llvmString(), getSource(0).getTypeString(),
                getSource(0).llvmString(), getResult().getTypeString());
    }

    @Override
    public List<Instruction> toArm() {
        return Collections.singletonList(new MovArmInstruction(getResult(),getSource(0)));
    }
}
