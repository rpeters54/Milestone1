package instructions;

public class AllocaInstruction implements Instruction {
    private final Register loc;

    public AllocaInstruction(Register loc) {
        this.loc = loc;
    }

    @Override
    public String toString() {
        // the register holds a pointer to the type being allocated
        // I need to remove the '*' character that would be generated
        String ptr = loc.getTypeString();
        String deref = ptr.substring(0, ptr.length()-1);
        // <result> = alloca <ty>
        return String.format("%s = alloca %s", loc.getValue(), deref);
    }
}
