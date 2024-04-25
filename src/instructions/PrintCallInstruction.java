package instructions;

public class PrintCallInstruction implements Instruction {
    private final Register dummy;
    private final Source printItem;
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
}
