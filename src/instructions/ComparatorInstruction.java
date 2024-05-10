package instructions;

import ast.expressions.BinaryExpression;
import ast.types.BoolType;

public class ComparatorInstruction implements FoldableInstruction {
    private Register result;
    private final BinaryExpression.Operator op;
    private Source left;
    private Source right;

    public ComparatorInstruction(Register result, BinaryExpression.Operator op, Source left, Source right) {
        this.result = result;
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public Register getResult() {
        return result;
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
        return String.format("%s = icmp %s %s %s, %s", result.getValue(), opName, left.getTypeString(), left.getValue(), right.getValue());
    }

    @Override
    public void substituteSource(Source original, Source replacement) {
        if (original.equals(result)) {
            if (replacement instanceof Register) {
//                replacement.setLabel(result.getLabel());
                result = (Register) replacement;
            }
            throw new RuntimeException("ComparatorInstruction: Tried to replace necessary Register with Source");
        }
        if (original.equals(left)) {
//            replacement.setLabel(left.getLabel());
            left = replacement;
        }
        if (original.equals(right)) {
//            replacement.setLabel(right.getLabel());
            right = replacement;
        }
    }


    @Override
    public void substituteLabel(Label original, Label replacement) {
        if (result.getLabel().equals(original))
            result.setLabel(replacement);
        if (left.getLabel().equals(original))
            left.setLabel(replacement);
        if (right.getLabel().equals(original))
            right.setLabel(replacement);
    }

    // if both sources are literals, evaluate the comparator and return a literal representing the value
    public Literal fold() {
        if (!(left instanceof Literal && right instanceof Literal)) {
            return null;
        }

        Literal result = new Literal(new BoolType(), null, left.getLabel());
        Literal leftConst = (Literal) left;
        Literal rightConst = (Literal) right;

        int leftVal = Integer.parseInt(leftConst.getValue());
        int rightVal = Integer.parseInt(rightConst.getValue());

        switch (op) {
            case LT -> result.setValue(Boolean.toString(leftVal < rightVal));
            case GT -> result.setValue(Boolean.toString(leftVal > rightVal));
            case LE -> result.setValue(Boolean.toString(leftVal <= rightVal));
            case GE -> result.setValue(Boolean.toString(leftVal >= rightVal));
            case EQ -> result.setValue(Boolean.toString(leftVal == rightVal));
            case NE -> result.setValue(Boolean.toString(leftVal != rightVal));
            default -> throw new RuntimeException("fold: Couldn't Resolve Binary Operator");
        }
        return result;
    }

}
