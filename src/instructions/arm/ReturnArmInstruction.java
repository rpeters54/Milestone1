package instructions.arm;


import instructions.Source;

import java.util.ArrayList;

public class ReturnArmInstruction extends AbstractArmInstruction {

    public ReturnArmInstruction() {
        super(new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public String toString() {
        return "ret";
    }
}
