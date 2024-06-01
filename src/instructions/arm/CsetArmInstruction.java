package instructions.arm;

import ast.expressions.BinaryExpression;
import instructions.Register;

import java.util.ArrayList;

public class CsetArmInstruction extends AbstractArmInstruction {

    private BinaryExpression.Operator op;

    public CsetArmInstruction(Register result, BinaryExpression.Operator op) {
        super(result, new ArrayList<>());
        this.op = op;
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
        return String.format("cset %s, %s", getResult(), cond);
    }
}
