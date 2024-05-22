package instructions.llvm;

import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;
import instructions.arm.LoadArmInstruction;
import instructions.arm.StoreArmInstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StoreLLVMInstruction extends AbstractLLVMInstruction implements CriticalInstruction {


    public StoreLLVMInstruction(Source obj, Source location) {
        super(null, Arrays.asList(obj, location));
    }

    @Override
    public String toString() {
        String deref = TypeMap.deref(getSource(1).getTypeString());
        return String.format("store %s %s, %s %s",
                deref, getSource(0),
                getSource(1).getTypeString(), getSource(1));
    }


    @Override
    public List<ArmInstruction> toArm() {
        return Collections.singletonList(new StoreArmInstruction(getSource(0), getSource(1)));
    }


}
