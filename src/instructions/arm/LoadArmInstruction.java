package instructions.arm;

import instructions.Register;
import instructions.Source;

public class LoadArmInstruction extends AbstractArmInstruction {

    public LoadArmInstruction(Register result, Source operand) {
        super(result, operand);
    }

    @Override
    public String toString() {
        return String.format("ldr %s, [%s]", getResult(), getSource(0));
    }
}
