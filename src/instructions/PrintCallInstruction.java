package instructions;

public class PrintCallInstruction implements Instruction {
    private Register dummy;
    private Source printItem;
    private final Boolean newLine;

    public PrintCallInstruction(Register dummy, Source printItem, Boolean newLine) {
        this.dummy = dummy;
        this.printItem = printItem;
        this.newLine = newLine;
    }

    @Override
    public String toString() {
        if (newLine) {
            return String.format("%s = call i64 (i8*, ...) " +
                    "@printf(i8* getelementptr inbounds ([5 x i8], " +
                    "[5 x i8]* @.println, i32 0, i32 0), %s %s)", dummy.getValue(),
                    printItem.getTypeString(), printItem.getValue());
        }
        return String.format("%s = call i64 (i8*, ...) " +
                "@printf(i8* getelementptr inbounds ([5 x i8], " +
                "[5 x i8]* @.print, i32 0, i32 0), %s %s)", dummy.getValue(),
                printItem.getTypeString(), printItem.getValue());
    }
    @Override
    public void substitute(Source item, Source replacement) {
        if (item.equals(dummy)) {
            if (replacement instanceof Register) {
                replacement.setLabel(dummy.getLabel());
                dummy = (Register) replacement;
            }
            throw new RuntimeException("PrintCallInstruction: Tried to replace necessary Register with Source");
        }
        if (item.equals(printItem)) {
            replacement.setLabel(printItem.getLabel());
            printItem = replacement;
        }
    }
}
