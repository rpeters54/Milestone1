package instructions.arm;

import ast.Function;
import instructions.Register;
import instructions.Source;

import java.util.ArrayList;
import java.util.List;

public class BranchLinkArmInstruction extends AbstractArmInstruction {

    private String functionName;

    public BranchLinkArmInstruction(String functionName, int numArgs) {
        super(Register.genArmCallRegisterList(), new ArrayList<>());
        List<Register> argList = Register.genArmArgRegisterList(numArgs);
        // have to do this nonsense because java is weird about generic types in lists
        List<Source> argSourceList = new ArrayList<>(argList);
        super.setSources(argSourceList);

        this.functionName = functionName;
    }

    @Override
    public String toString() {
        return String.format("bl %s", functionName);
    }
}
