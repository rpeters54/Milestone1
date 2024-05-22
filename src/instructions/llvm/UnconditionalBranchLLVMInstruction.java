package instructions.llvm;

import instructions.Label;
import instructions.arm.ArmInstruction;

import java.util.ArrayList;
import java.util.List;

public class UnconditionalBranchLLVMInstruction extends AbstractLLVMInstruction implements JumpInstruction {
    private Label stub;

    public UnconditionalBranchLLVMInstruction(Label stub) {
        super(null, new ArrayList<>());
        this.stub = stub;
    }

    public Label getStub() {
        return stub;
    }

    @Override
    public String toString() {
        return String.format("br label %%%s", stub.getName());
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        if (stub.equals(original))
            stub = replacement;
    }

    @Override
    public List<ArmInstruction> toArm() {
        throw new RuntimeException("Fix this first");
    }
}
