package instructions.arm;

import ast.expressions.BinaryExpression;
import instructions.Register;
import instructions.Source;

public class BinaryArmInstruction extends AbstractArmInstruction {

    private BinaryExpression.Operator op;

    public BinaryArmInstruction(Register result, BinaryExpression.Operator op, Source left, Source right) {
        super(result, left, right);
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
            case OR -> "orr";
            case XOR -> "eor";
            default -> throw new IllegalArgumentException("Bad Binop Name");
        };

        // <op> <result>, <reg1>, <operand2>
        return String.format("%s %s, %s, %s", opName, getResult(), getSource(0), getSource(1));
    }

}
