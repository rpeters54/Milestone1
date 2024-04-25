package instructions;

import ast.expressions.BinaryExpression;

public class BinaryInstruction implements Instruction {
    private final Register lVal;
    private final BinaryExpression.Operator op;
    private final Source left;
    private final Source right;

    public BinaryInstruction(Register lVal, BinaryExpression.Operator op, Source left, Source right) {
        this.lVal = lVal;
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {

        String opName = switch (op) {
            case TIMES -> "mul";
            case DIVIDE -> "sdiv";
            case PLUS -> "add";
            case MINUS -> "sub";
            case AND -> "and";
            case OR -> "or";
            case XOR -> "xor";
            default -> throw new IllegalArgumentException("Bad Binop Name");
        };

        // <result> = <op> <type> <operand1>, <operand2>
        return String.format("%s = %s %s %s, %s", lVal.getValue(), opName, left.getTypeString(), left.getValue(), right.getValue());
    }
}
