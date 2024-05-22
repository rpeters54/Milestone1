package instructions.arm;

import instructions.Register;
import instructions.Source;

public class MovArmInstruction extends AbstractArmInstruction {

    public MovArmInstruction(Register result, Source operand) {
        super(result, operand);
    }

    @Override
    public String toString() {
        return String.format("MOV %s, %s", getResult(), getSource(0));
    }
}
