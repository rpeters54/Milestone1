package instructions.llvm;

import instructions.Instruction;
import instructions.Register;
import instructions.Source;
import instructions.arm.LoadLabelArmInstruction;
import instructions.arm.LoadRegisterArmInstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoadLLVMInstruction extends AbstractLLVMInstruction implements CriticalInstruction {


    public LoadLLVMInstruction(Register result, Source loc) {
        super(result, new ArrayList<>());
        addSource(loc);
    }

    private Source loc() {
        return super.getSource(0);
    }

    @Override
    public String toString() {
        String deref = TypeMap.deref(loc().getTypeString());
        // <result> = load <ty>, <ty>* <source>
        return String.format("%s = load %s, %s %s",
                getResult().llvmString(), deref, loc().getTypeString(), loc().llvmString());
    }

    @Override
    public List<Instruction> toArm() {
        if (loc() instanceof Register && ((Register) loc()).isGlobal()) {
            return Arrays.asList(
                    new LoadLabelArmInstruction(getResult(), loc().toString()),
                    new LoadRegisterArmInstruction(getResult(), getResult())
            );
        } else {
            return Collections.singletonList(new LoadRegisterArmInstruction(getResult(), getSource(0)));
        }
    }
}
