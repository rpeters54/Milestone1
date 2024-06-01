package instructions.arm;

import instructions.Register;

import java.util.ArrayList;

public class LoadOffsetArmInstruction extends AbstractArmInstruction{

    private int offset;

    public LoadOffsetArmInstruction(Register result, int offset) {
        super(result, Register.genStackPointer());
        this.offset = offset;
    }

    @Override
    public String toString() {
        return String.format("ldr %s, [sp, %d]", getResult(), offset);
    }
}