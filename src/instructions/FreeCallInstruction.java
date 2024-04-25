package instructions;

public class FreeCallInstruction implements Instruction {
    private final Register result;
    private final Source ptr;

    public FreeCallInstruction(Register result, Source ptr) {
        this.result = result;
        this.ptr = ptr;
    }

    @Override
    public String toString() {
        return String.format("%s = void @free(i8* %s)",
                result.getValue(), ptr.getValue());
    }
}
