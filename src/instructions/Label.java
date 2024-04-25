package instructions;

public class Label implements Instruction {
    private final String value;

    private static int labelCount = 0;

    public Label(String value) {
        this.value = value;
    }

    public Label() {
        this.value = "lab"+labelCount++;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s:", value);
    }
}
