package instructions;

import ast.expressions.BinaryExpression;

public class BinaryInstruction implements Instruction {
    private Register lVal;
    private final BinaryExpression.Operator op;
    private Source left;
    private Source right;

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

    @Override
    public void substitute(Source item, Source replacement) {
        if (item.equals(lVal)) {
            if (replacement instanceof Register) {
                replacement.setLabel(lVal.getLabel());
                lVal = (Register) replacement;
            }
            throw new RuntimeException("BinaryInstruction: Tried to replace necessary Register with Source");
        }
        if (item.equals(left)) {
            replacement.setLabel(left.getLabel());
            left = replacement;
        }
        if (item.equals(right)) {
            replacement.setLabel(right.getLabel());
            right = replacement;
        }
    }
}
