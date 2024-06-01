package instructions.arm;

import instructions.Instruction;
import instructions.Register;
import instructions.Source;

import java.util.ArrayList;
import java.util.List;

public class MovArmInstruction extends AbstractArmInstruction {

    private String driverName;

    public MovArmInstruction(Register result, Source operand) {
        super(result, operand);
        this.driverName = null;
    }

    /* Used to allow for primitive functions to use calling conventions */
    public MovArmInstruction(Register result, String driverName) {
        super(result, new ArrayList<>());
        this.driverName = driverName;
    }

    /**
     * Used when a llvm instruction has a literal in the left position of a binary expression / comparison
     * This adds an intermediate mov instruction so that the instruction conforms to arm standards
     * @param source
     * @param instructions
     * @return
     */
    public static Register optionalMov(Source source, List<Instruction> instructions) {
        if (source instanceof Register) {
            return (Register) source;
        }

        Register intermediateReg = Register.genTypedLocalRegister(source.getType().copy(), source.getLabel());
        instructions.add(new MovArmInstruction(intermediateReg, source));
        return intermediateReg;
    }

    @Override
    public String toString() {
        if (driverName != null) {
            return String.format("mov %s, %s", getResult(), driverName);
        } else {
            return String.format("mov %s, %s", getResult(), getSource(0));
        }
    }
}
