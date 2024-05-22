package instructions.arm;

import instructions.Register;
import instructions.Source;

import java.util.ArrayList;

public class StoreArmInstruction extends AbstractArmInstruction {

    public StoreArmInstruction(Source value, Source location) {
        super(new ArrayList<>(), value, location);
    }

    @Override
    public String toString() {
        return String.format("str %s, [%s]", getSource(0), getSource(1));
    }
}
