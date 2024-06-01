package instructions.llvm;

import instructions.Instruction;
import instructions.Register;
import instructions.arm.BranchLinkArmInstruction;
import instructions.arm.LoadLabelArmInstruction;
import instructions.arm.LoadRegisterArmInstruction;
import instructions.arm.MovArmInstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReadCallLLVMInstruction extends AbstractLLVMInstruction implements CriticalInstruction {

    public ReadCallLLVMInstruction(Register result, Register dummy) {
        super(result, Collections.singletonList(dummy));
    }

    @Override
    public String toString() {
        return String.format("%s = call i64 (i8*, ...) @scanf(i8* getelementptr " +
                "inbounds ([4 x i8], [4 x i8]* @.read, i64 0, " +
                "i64 0), i64* @.read_scratch)\n"+
                "%s = load %s, i64* @.read_scratch", getSource(0).llvmString(),
                getResult().llvmString(), getResult().getTypeString());
    }

    @Override
    public List<Instruction> toArm() {
        List<Instruction> instList = new ArrayList<>();
        instList.add(new LoadLabelArmInstruction(Register.genArmRegister(0), "read"));
        instList.add(new LoadLabelArmInstruction(Register.genArmRegister(1), "read_scratch"));
        instList.add(new BranchLinkArmInstruction("scanf", 2));
        instList.add(new MovArmInstruction((Register) getSource(0), Register.genArmRegister(0)));
        instList.add(new LoadLabelArmInstruction(getResult(), "read_scratch"));
        instList.add(new LoadRegisterArmInstruction(getResult(), getResult()));
        return instList;
    }
}
