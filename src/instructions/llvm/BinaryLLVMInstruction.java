package instructions.llvm;

import ast.expressions.BinaryExpression;
import ast.types.BoolType;
import ast.types.IntType;
import instructions.Literal;
import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;
import instructions.arm.BinaryArmInstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BinaryLLVMInstruction extends AbstractLLVMInstruction implements FoldableInstruction {
    private final BinaryExpression.Operator op;

    public BinaryLLVMInstruction(Register result, BinaryExpression.Operator op, Source left, Source right) {
        super(result, new ArrayList<>(Arrays.asList(left, right)));
        this.op = op;
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
        return String.format("%s = %s %s %s, %s", getResult(), opName,
                getSource(0).getTypeString(), getSource(0), getSource(1));
    }

    // if both sources are literals, evaluate the binary expression and return a literal representing the value
    public Literal fold() {
        if (!(getSource(0) instanceof Literal && getSource(1) instanceof Literal)) {
            return null;
        }

        Literal result = new Literal(null, null, getSource(0).getLabel());
        Literal leftConst = (Literal) getSource(0);
        Literal rightConst = (Literal) getSource(1);

        switch (op) {
            case TIMES, DIVIDE, MINUS, PLUS -> {
                int leftVal = Integer.parseInt(leftConst.toString());
                int rightVal = Integer.parseInt(rightConst.toString());
                switch (op) {
                    case TIMES -> result.setValue(Integer.toString(leftVal*rightVal));
                    case DIVIDE -> result.setValue(Integer.toString(leftVal/rightVal));
                    case MINUS -> result.setValue(Integer.toString(leftVal-rightVal));
                    case PLUS -> result.setValue(Integer.toString(leftVal+rightVal));
                }
                result.setType(new IntType());
            }
            case AND, OR, XOR -> {
                boolean leftVal = leftConst.toString().equals("true");
                boolean rightVal = rightConst.toString().equals("true");
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

    @Override
    public List<ArmInstruction> toArm() {
        BinaryArmInstruction binop = new BinaryArmInstruction(getResult(), op, getSource(0), getSource(1));
        return Collections.singletonList(binop);
    }
}
