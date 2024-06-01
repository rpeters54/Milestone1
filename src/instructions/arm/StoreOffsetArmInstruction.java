package instructions.arm;

import instructions.Register;
import instructions.Source;

import java.util.ArrayList;

public class StoreOffsetArmInstruction  extends AbstractArmInstruction {

    private int offset;

    public StoreOffsetArmInstruction(Source value, int offset) {
        super(new ArrayList<>(), value, Register.genStackPointer());
        this.offset = offset;
    }

    @Override
    public String toString() {
        return String.format("str %s, [sp, %d]", getSource(0), offset);
    }
}
