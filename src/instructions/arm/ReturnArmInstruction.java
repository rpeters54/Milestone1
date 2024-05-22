package instructions.arm;


import instructions.Source;

import java.util.ArrayList;

public class ReturnArmInstruction extends AbstractArmInstruction {

    public ReturnArmInstruction(Source operand) {
        super(new ArrayList<>(), operand);
    }

    public ReturnArmInstruction() {
        super(new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public String toString() {
        if (getSources().size() > 0)
            return String.format("ret %s", getSource(0));
        return "ret";
    }
}
