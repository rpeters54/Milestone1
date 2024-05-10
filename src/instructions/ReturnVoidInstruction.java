package instructions;

public class ReturnVoidInstruction implements JumpInstruction {
    @Override
    public String toString() {
        return "ret void";
    }

    @Override
    public void substituteSource(Source original, Source replacement) {
        //do nothing
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        //do nothing
    }
}
