package instructions.llvm;

import instructions.Register;
import instructions.arm.ArmInstruction;

import java.util.ArrayList;
import java.util.List;

public class ReadCallLLVMInstruction extends AbstractLLVMInstruction implements CriticalInstruction {

    public ReadCallLLVMInstruction(Register result, Register store) {
        super(result, new ArrayList<>());
        addSource(store);
    }

    @Override
    public String toString() {
        return String.format("%s = call i64 (i8*, ...) @scanf(i8* getelementptr " +
                "inbounds ([4 x i8], [4 x i8]* @.read, i64 0, " +
                "i64 0), i64* @.read_scratch)\n"+
                "%s = load %s, i64* @.read_scratch", getResult(),
                getSource(0), getSource(0).getTypeString());
    }

    @Override
    public List<ArmInstruction> toArm() {
        throw new RuntimeException("Fix this first");
    }
}
