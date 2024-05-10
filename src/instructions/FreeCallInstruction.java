package instructions;

public class FreeCallInstruction implements Instruction {
    private Source ptr;

    public FreeCallInstruction(Source ptr) {
        this.ptr = ptr;
    }

    @Override
    public String toString() {
        return String.format("call void @free(i8* %s)", ptr.getValue());
    }

    @Override
    public void substituteSource(Source original, Source replacement) {
        if (ptr.equals(original)) {
//            replacement.setLabel(ptr.getLabel());
            ptr = replacement;
        }
    }

    @Override
    public void substituteLabel(Label original, Label replacement) {
        if (ptr.getLabel().equals(original))
            ptr.setLabel(replacement);
    }
}
