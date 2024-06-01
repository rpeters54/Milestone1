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

    public Label getDestination() {
        return stub;
    }

    public void setDestination(Label dest) {
        stub = dest;
    }

    @Override
    public String toString() {
        return String.format("b %s", stub.getName());
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        super.substituteLabel(original, replacement);
        if (stub.equals(original)) {
            stub = replacement;
        }
    }
}
