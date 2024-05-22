package instructions.llvm;

import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;

import java.util.ArrayList;
import java.util.List;

public class PrintCallLLVMInstruction extends AbstractLLVMInstruction implements CriticalInstruction {
    private final Boolean newLine;

    public PrintCallLLVMInstruction(Register result, Source printItem, Boolean newLine) {
        super(result, new ArrayList<>());
        addSource(printItem);
        this.newLine = newLine;
    }

    private Source printItem() {
        return super.getSource(0);
    }

    @Override
    public String toString() {
        if (newLine) {
            return String.format("%s = call i64 (i8*, ...) " +
                            "@printf(i8* getelementptr inbounds ([5 x i8], " +
                            "[5 x i8]* @.println, i32 0, i32 0), %s %s)", getResult(),
                    printItem().getTypeString(), printItem());
        }
        return String.format("%s = call i64 (i8*, ...) " +
                        "@printf(i8* getelementptr inbounds ([5 x i8], " +
                        "[5 x i8]* @.print, i32 0, i32 0), %s %s)", getResult(),
                printItem().getTypeString(), printItem());
    }

    @Override
    public List<ArmInstruction> toArm() {
        throw new RuntimeException("Fix this first");
    }
}
