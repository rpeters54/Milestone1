package instructions.llvm;

import instructions.Instruction;
import instructions.Literal;
import instructions.Register;
import instructions.Source;
import instructions.arm.LoadLabelArmInstruction;
import instructions.arm.MovArmInstruction;
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
                deref, getSource(0).llvmString(),
                getSource(1).getTypeString(), getSource(1).llvmString());
    }


    @Override
    public List<Instruction> toArm() {
        List<Instruction> instList = new ArrayList<>();
        // if storing a literal, add an explicit move
        Register objReg;
        if (getSource(0) instanceof Literal) {
            objReg = Register.genTypedLocalRegister(getSource(0).getType().copy(), getSource(0).getLabel());
            instList.add(new MovArmInstruction(objReg, getSource(0)));
        } else {
            objReg = (Register) getSource(0);
        }
        // if storing to a global, load the address before storing
        if (getSource(1) instanceof Register && ((Register) getSource(1)).isGlobal()) {
            Register temp = Register.genTypedLocalRegister(getSource(1).getType().copy(), getSource(1).getLabel());
            instList.add(new LoadLabelArmInstruction(temp, getSource(1).toString()));
            instList.add(new StoreArmInstruction(objReg, temp));
        } else {
            instList.add(new StoreArmInstruction(objReg, getSource(1)));

        }
        return instList;
    }


}
