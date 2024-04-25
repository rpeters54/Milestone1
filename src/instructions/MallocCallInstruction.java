package instructions;

public class MallocCallInstruction implements Instruction {
    private Register result;
    private Source size;

    public MallocCallInstruction(Register result, Source size) {
        this.result = result;
        this.size = size;
    }

    @Override
    public String toString() {
        return String.format("%s = call i8* @malloc(%s %s)",
                result.getValue(), size.getTypeString(), size.getValue());
    }
}
