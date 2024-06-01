package instructions.llvm;

import instructions.Instruction;
import instructions.Register;
import instructions.Source;
import instructions.arm.BranchLinkArmInstruction;
import instructions.arm.LoadLabelArmInstruction;
import instructions.arm.LoadRegisterArmInstruction;
import instructions.arm.MovArmInstruction;

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
                            "[5 x i8]* @.println, i32 0, i32 0), %s %s)", getResult().llvmString(),
                    printItem().getTypeString(), printItem().llvmString());
        }
        return String.format("%s = call i64 (i8*, ...) " +
                        "@printf(i8* getelementptr inbounds ([5 x i8], " +
                        "[5 x i8]* @.print, i32 0, i32 0), %s %s)", getResult().llvmString(),
                printItem().getTypeString(), printItem().llvmString());
    }

    @Override
    public List<Instruction> toArm() {
        List<Instruction> instList = new ArrayList<>();
        String container = newLine ? "println" : "print";
        instList.add(new LoadLabelArmInstruction(Register.genArmRegister(0), container));
        instList.add(new MovArmInstruction(Register.genArmRegister(1), printItem()));
        instList.add(new BranchLinkArmInstruction("printf", 2));
        instList.add(new MovArmInstruction(getResult(), Register.genArmRegister(0)));
        return instList;
    }
}
