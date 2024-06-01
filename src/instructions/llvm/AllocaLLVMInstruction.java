package instructions.llvm;

import instructions.Instruction;
import instructions.Register;
import instructions.arm.ArmInstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllocaLLVMInstruction extends AbstractLLVMInstruction {

    public AllocaLLVMInstruction(Register result) {
        super(result, new ArrayList<>());
    }

    @Override
    public String toString() {
        // the register holds a pointer to the type being allocated
        // I need to remove the '*' character that would be generated
        String ptr = getResult().getTypeString();
        String deref = ptr.substring(0, ptr.length()-1);
        // <result> = alloca <ty>
        return String.format("%s = alloca %s", getResult().llvmString(), deref);
    }

    @Override
    public List<Instruction> toArm() {
        return Collections.singletonList(new AllocaLLVMInstruction(getResult()));
    }
}
