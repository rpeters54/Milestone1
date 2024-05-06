package instructions;

public class UnconditionalBranchInstruction implements JumpInstruction {
    private final Label stub;

    public UnconditionalBranchInstruction(Label stub) {
        this.stub = stub;
    }

    @Override
    public String toString() {
        return String.format("br label %%%s", stub.getValue());
    }

    @Override
    public void substitute(Source item, Source replacement) {
        //do nothing
    }
}
