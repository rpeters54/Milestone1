package instructions.arm;

import instructions.Source;

import java.util.ArrayList;
import java.util.Arrays;

public class CmpArmInstruction extends AbstractArmInstruction{

    public CmpArmInstruction(Source left, Source right) {
        super(new ArrayList<>(), left, right);
    }

    @Override
    public String toString() {
        return String.format("CMP %s, %s", getSource(0), getSource(1));
    }
}
