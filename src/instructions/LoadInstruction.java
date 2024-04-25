package instructions;

public class LoadInstruction implements Instruction {
    private final Register lVal;
    private final Source loc;

    public LoadInstruction(Register lVal, Source loc) {
        this.lVal = lVal;
        this.loc = loc;
    }

    @Override
    public String toString() {
        String deref = TypeMap.deref(loc.getTypeString());
        // <result> = load <ty>, <ty>* <source>
        return String.format("%s = load %s, %s %s",
                lVal.getValue(), deref, loc.getTypeString(), loc.getValue());
    }
}
