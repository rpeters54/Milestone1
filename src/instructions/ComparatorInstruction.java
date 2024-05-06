package instructions;

import ast.expressions.BinaryExpression;

public class ComparatorInstruction implements Instruction {
    private Register lVal;
    private final BinaryExpression.Operator op;
    private Source left;
    private Source right;

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

    @Override
    public void substitute(Source item, Source replacement) {
        if (item.equals(lVal)) {
            if (replacement instanceof Register) {
                replacement.setLabel(lVal.getLabel());
                lVal = (Register) replacement;
            }
            throw new RuntimeException("ComparatorInstruction: Tried to replace necessary Register with Source");
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
