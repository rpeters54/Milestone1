package instructions;

public class StoreInstruction implements Instruction {
    private final Register loc;
    private final Source item;

    public StoreInstruction(Register loc, Source item) {
        this.loc = loc;
        this.item = item;
    }

    @Override
    public String toString() {
        String deref = TypeMap.deref(loc.getTypeString());
        return String.format("store %s %s, %s %s",
                deref, item.getValue(),
                loc.getTypeString(), loc.getValue());
    }
}
