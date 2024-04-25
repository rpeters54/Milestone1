package instructions;

public class ReadCallInstruction implements Instruction {
    private final Register dummy;
    private final Register result;

    public ReadCallInstruction(Register dummy, Register result) {
        this.dummy = dummy;
        this.result = result;
    }

    @Override
    public String toString() {


        return String.format("%s = call i64 (i8*, ...) @scanf(i8* getelementptr " +
                "inbounds ([4 x i8], [4 x i8]* @.read, i64 0, " +
                "i64 0), i64* @.read_scratch)\n"+
                "%s = load %s, i64* @.read_scratch", dummy.getValue(), result.getValue(), result.getTypeString());
    }
}
