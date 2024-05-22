package instructions.llvm;

import ast.expressions.BinaryExpression;
import ast.types.IntType;
import instructions.Label;
import instructions.Literal;
import instructions.Source;
import instructions.arm.*;

import java.util.ArrayList;
import java.util.List;

public class ConditionalBranchLLVMInstruction extends AbstractLLVMInstruction implements JumpInstruction {
    private Label trueStub;
    private Label falseStub;

    public ConditionalBranchLLVMInstruction(Source cond, Label trueStub, Label falseStub) {
        super(null, new ArrayList<>());
        addSource(cond);
        this.trueStub = trueStub;
        this.falseStub = falseStub;
    }

    public Label getTrueStub() {
        return trueStub;
    }

    public Label getFalseStub() {
        return falseStub;
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        super.substituteLabel(original, replacement);
        if (trueStub.equals(original))
            trueStub = replacement;
        if (falseStub.equals(original))
            falseStub = replacement;
    }

    @Override
    public String toString() {
        return String.format("br %s %s, label %%%s, label %%%s",
                getSource(0).getTypeString(), getSource(0),
                trueStub.getName(), falseStub.getName());
    }

    @Override
    public List<ArmInstruction> toArm() {
        List<ArmInstruction> instList = new ArrayList<>();
        instList.add(new CmpArmInstruction(getSource(0), new Literal(new IntType(), "0", getSource(0).getLabel())));
        instList.add(new ConditionalBranchArmInstruction(trueStub, BinaryExpression.Operator.NE));
        instList.add(new UnconditionalBranchArmInstruction(falseStub));
        return instList;
    }
}
