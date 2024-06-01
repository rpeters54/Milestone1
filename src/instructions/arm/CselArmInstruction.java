package instructions.arm;

import ast.expressions.BinaryExpression;
import instructions.Register;
import instructions.Source;


public class CselArmInstruction extends AbstractArmInstruction {

    private String op;

    public CselArmInstruction(Register result, Source left, Source right, String op) {
        super(result, left, right);
        this.op = op;
    }

    @Override
    public String toString() {
        return String.format("csel %s, %s, %s, %s", getResult(), getSource(0), getSource(1), op);
    }
}
