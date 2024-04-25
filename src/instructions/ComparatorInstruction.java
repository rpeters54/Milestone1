package instructions;

import ast.expressions.BinaryExpression;

public class ComparatorInstruction implements Instruction {
    private final Register lVal;
    private final Source left;
    private final Source right;
    private final BinaryExpression.Operator op;

    public ComparatorInstruction(Register lVal, BinaryExpression.Operator op, Source left, Source right) {
        this.lVal = lVal;
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {

        String opName = switch (op) {
            case LT -> "slt";
            case GT -> "sgt";
            case LE -> "sle";
            case GE -> "sge";
            case EQ -> "eq";
            case NE -> "ne";
            default -> throw new IllegalArgumentException("Bad Comp Name");
        };

        // <result> = icmp <cond> <type> <operand1>, <operand2>
        return String.format("%s = icmp %s %s %s, %s", lVal.getValue(), opName, left.getTypeString(), left.getValue(), right.getValue());
    }

}
