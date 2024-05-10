package instructions;

import ast.expressions.BinaryExpression;
import ast.types.BoolType;
import ast.types.IntType;

public class BinaryInstruction implements FoldableInstruction {
    private Register result;
    private final BinaryExpression.Operator op;
    private Source left;
    private Source right;

    public BinaryInstruction(Register result, BinaryExpression.Operator op, Source left, Source right) {
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
        return String.format("%s = %s %s %s, %s", result.getValue(), opName, left.getTypeString(), left.getValue(), right.getValue());
    }

    @Override
    public void substituteSource(Source original, Source replacement) {
        if (original.equals(result)) {
            if (replacement instanceof Register) {
//                replacement.setLabel(result.getLabel());
                result = (Register) replacement;
            }
            throw new RuntimeException("BinaryInstruction: Tried to replace necessary Register with Source");
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

    // if both sources are literals, evaluate the binary expression and return a literal representing the value
    public Literal fold() {
        if (!(left instanceof Literal && right instanceof Literal)) {
            return null;
        }

        Literal result = new Literal(null, null, left.getLabel());
        Literal leftConst = (Literal) left;
        Literal rightConst = (Literal) right;

        switch (op) {
            case TIMES, DIVIDE, MINUS, PLUS -> {
                int leftVal = Integer.parseInt(leftConst.getValue());
                int rightVal = Integer.parseInt(rightConst.getValue());
                switch (op) {
                    case TIMES -> result.setValue(Integer.toString(leftVal*rightVal));
                    case DIVIDE -> result.setValue(Integer.toString(leftVal/rightVal));
                    case MINUS -> result.setValue(Integer.toString(leftVal-rightVal));
                    case PLUS -> result.setValue(Integer.toString(leftVal+rightVal));
                }
                result.setType(new IntType());
            }
            case AND, OR, XOR -> {
                boolean leftVal = leftConst.getValue().equals("true");
                boolean rightVal = rightConst.getValue().equals("true");
                switch (op) {
                    case AND -> result.setValue(Boolean.toString(leftVal && rightVal));
                    case OR -> result.setValue(Boolean.toString(leftVal || rightVal));
                    case XOR -> result.setValue(Boolean.toString(leftVal ^ rightVal));
                }
                result.setType(new BoolType());
            }
            default -> throw new RuntimeException("fold: Couldn't Resolve Binary Operator");
        }
        return result;
    }
}
