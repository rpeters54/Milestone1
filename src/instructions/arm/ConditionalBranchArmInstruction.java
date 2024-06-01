package instructions.arm;

import ast.expressions.BinaryExpression;
import instructions.Label;

import java.util.ArrayList;

public class ConditionalBranchArmInstruction extends AbstractArmInstruction {

    private Label trueStub;
    private BinaryExpression.Operator op;

    public ConditionalBranchArmInstruction(Label trueStub, BinaryExpression.Operator op) {
        super(new ArrayList<>(), new ArrayList<>());
        this.trueStub = trueStub;
        this.op = op;
    }

    public Label getDestination() {
        return trueStub;
    }

    public void setDestination(Label dest) {
        trueStub = dest;
    }

    @Override
    public String toString() {
        String cond = switch(op) {
            case LT -> "lt";
            case GT -> "gt";
            case LE -> "le";
            case GE -> "ge";
            case EQ -> "eq";
            case NE -> "ne";
            default -> throw new RuntimeException("CsetArmInstruction::toString: invalid operand");
        };
        return String.format("b%s %s", cond, trueStub.getName());
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        super.substituteLabel(original, replacement);
        if (trueStub.equals(original)) {
            trueStub = replacement;
        }
    }
}
