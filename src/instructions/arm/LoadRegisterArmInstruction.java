package instructions.arm;

import instructions.Register;
import instructions.Source;

import java.util.ArrayList;
import java.util.Arrays;

public class LoadRegisterArmInstruction extends AbstractArmInstruction {

    private int offset = 0;

    public LoadRegisterArmInstruction(Register result, Source operand) {
        super(result, operand);
    }

    public LoadRegisterArmInstruction(Register result, int offset) {
        super(result, Arrays.asList(Register.genArmRegister(13)));
        this.offset = offset;
    }

    @Override
    public String toString() {
        if (offset == 0) {
            return String.format("ldr %s, [%s]", getResult(), getSource(0));
        } else {
            return String.format("ldr %s, [%s, %d]", getResult(), getSource(0), offset);
        }
    }
}
