package instructions.llvm;

import ast.expressions.BinaryExpression;
import ast.types.BoolType;
import instructions.Literal;
import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;
import instructions.arm.CmpArmInstruction;
import instructions.arm.CsetArmInstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComparatorLLVMInstruction extends AbstractLLVMInstruction implements FoldableInstruction {
    private final BinaryExpression.Operator op;

    public ComparatorLLVMInstruction(Register result, BinaryExpression.Operator op, Source left, Source right) {
        super(result, new ArrayList<>(Arrays.asList(left, right)));
        this.op = op;
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
        return String.format("%s = icmp %s %s %s, %s", getResult(),
                opName, getSource(0).getTypeString(), getSource(0), getSource(1));
    }


    // if both sources are literals, evaluate the comparator and return a literal representing the value
    public Literal fold() {
        if (!(getSource(0) instanceof Literal && getSource(1) instanceof Literal)) {
            return null;
        }

        Literal result = new Literal(new BoolType(), null, getSource(0).getLabel());
        Literal leftConst = (Literal) getSource(0);
        Literal rightConst = (Literal) getSource(1);

        int leftVal = Integer.parseInt(leftConst.toString());
        int rightVal = Integer.parseInt(rightConst.toString());

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

    @Override
    public List<ArmInstruction> toArm() {
        List<ArmInstruction> instList = new ArrayList<>();
        instList.add(new CmpArmInstruction(getSource(0), getSource(1)));
        instList.add(new CsetArmInstruction(getResult(), op));
        return instList;
    }
}
