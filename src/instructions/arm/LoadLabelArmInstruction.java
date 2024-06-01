package instructions.arm;

import instructions.Register;

import java.util.ArrayList;

/* Exists to allow primitive functions to follow calling conventions */
public class LoadLabelArmInstruction extends AbstractArmInstruction{

    private String name;

    public LoadLabelArmInstruction(Register result, String name) {
        super(result, new ArrayList<>());
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("ldr %s, =%s", getResult(), name);
    }
}
