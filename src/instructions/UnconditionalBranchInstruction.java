package instructions;

public class UnconditionalBranchInstruction implements JumpInstruction {
    private Label stub;

    public UnconditionalBranchInstruction(Label stub) {
        this.stub = stub;
    }

    @Override
    public String toString() {
        return String.format("br label %%%s", stub.getValue());
    }

    @Override
    public void substituteSource(Source original, Source replacement) {
        //do nothing
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        if (stub.equals(original))
            stub = replacement;
    }
}
