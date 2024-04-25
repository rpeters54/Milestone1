package instructions;

public class GetElemPtrInstruction implements Instruction {
    private final Register lVal;
    private final Source item;
    private final Source index;

    public GetElemPtrInstruction(Register lVal, Source item, Source index) {
        this.lVal = lVal;
        this.item = item;
        this.index = index;
    }

    @Override
    public String toString() {
        String deref = TypeMap.deref(item.getTypeString());

        return String.format("%s = getelementptr inbounds %s, %s %s, i32 0, i32 %s",
                lVal.getValue(), deref, item.getTypeString(), item.getValue(), index.getValue());
    }
}
