package instructions.arm;

import ast.expressions.BinaryExpression;
import instructions.Label;

import java.util.ArrayList;

public class UnconditionalBranchArmInstruction extends AbstractArmInstruction {
    private Label stub;

    public UnconditionalBranchArmInstruction(Label trueStub) {
        super(new ArrayList<>(), new ArrayList<>());
        this.stub = trueStub;
    }

    @Override
    public String toString() {
        return String.format("b %s", stub);
    }
}
