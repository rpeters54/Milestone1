package instructions.llvm;

import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;

import java.util.ArrayList;
import java.util.List;

public class BitcastLLVMInstruction extends AbstractLLVMInstruction {


    public BitcastLLVMInstruction(Register result, Source uncasted) {
        super(result, new ArrayList<>());
        addSource(uncasted);
    }

    @Override
    public String toString() {
        return String.format("%s = bitcast %s %s to %s",
                getResult(), getSource(0).getTypeString(),
                getSource(0), getResult().getTypeString());
    }

    @Override
    public List<ArmInstruction> toArm() {
        return new ArrayList<>();
    }
}
