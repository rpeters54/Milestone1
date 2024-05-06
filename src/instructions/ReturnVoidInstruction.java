package instructions;

public class ReturnVoidInstruction implements JumpInstruction {
    @Override
    public String toString() {
        return "ret void";
    }

    @Override
    public void substitute(Source item, Source replacement) {
        //do nothing
    }
}
