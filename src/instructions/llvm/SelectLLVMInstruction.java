package instructions.llvm;

import ast.types.IntType;
import instructions.Literal;
import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;
import instructions.arm.CmpArmInstruction;
import instructions.arm.CselArmInstruction;

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
                result, cond.getTypeString(), cond,
                left.getTypeString(), left, right.getTypeString(), right);
    }

    @Override
    public List<ArmInstruction> toArm() {
        List<ArmInstruction> instList = new ArrayList<>();
        instList.add(new CmpArmInstruction(getSource(0),
                new Literal(new IntType(), "0", getSource(0).getLabel())));
        instList.add(new CselArmInstruction(getResult(), getSource(1), getSource(2), "ne"));
        return instList;
    }
}
