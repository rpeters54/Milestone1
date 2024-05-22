package instructions.llvm;

import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;
import instructions.arm.LoadArmInstruction;

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
                getResult(), deref, loc().getTypeString(), loc());
    }

    @Override
    public List<ArmInstruction> toArm() {
        return Collections.singletonList(new LoadArmInstruction(getResult(), getSource(0)));
    }
}
