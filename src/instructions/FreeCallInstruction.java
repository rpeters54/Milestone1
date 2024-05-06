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
    public void substitute(Source item, Source replacement) {
        if (ptr.equals(item)) {
            replacement.setLabel(ptr.getLabel());
            ptr = replacement;
        }
    }
}
