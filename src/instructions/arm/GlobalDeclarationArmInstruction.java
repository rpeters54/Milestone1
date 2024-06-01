package instructions.arm;

import instructions.llvm.GlobalRegisterDeclarationLLVMInstruction;

import java.util.ArrayList;

public class GlobalDeclarationArmInstruction extends AbstractArmInstruction {

    private String name;

    public GlobalDeclarationArmInstruction(String name) {
        super(new ArrayList<>(), new ArrayList<>());
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format(".global %s\n%s:\n    .xword 0x0\n", name, name);
    }
}
