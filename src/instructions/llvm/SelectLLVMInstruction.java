package instructions.llvm;

import ast.types.IntType;
import instructions.Instruction;
import instructions.Literal;
import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;
import instructions.arm.CmpArmInstruction;
import instructions.arm.CselArmInstruction;
import instructions.arm.MovArmInstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SelectLLVMInstruction extends AbstractLLVMInstruction {

    public SelectLLVMInstruction(Register result, Source cond, Source left, Source right) {
        super(result, Arrays.asList(cond, left, right));
    }

    @Override
    public String toString() {
        Register result = getResult();
        Source cond = getSource(0);
        Source left = getSource(1);
        Source right = getSource(2);

        return String.format("%s = select %s %s, %s %s, %s %s",
                result.llvmString(), cond.getTypeString(), cond.llvmString(),
                left.getTypeString(), left.llvmString(), right.getTypeString(), right.llvmString());
    }

    @Override
    public List<Instruction> toArm() {
        List<Instruction> instList = new ArrayList<>();
        Register leftReg = MovArmInstruction.optionalMov(getSource(0), instList);
        instList.add(new CmpArmInstruction(leftReg,
                new Literal(new IntType(), "0", getSource(0).getLabel())));
        instList.add(new CselArmInstruction(getResult(), getSource(1), getSource(2), "ne"));
        return instList;
    }
}
