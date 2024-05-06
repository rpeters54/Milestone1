package instructions;

import java.util.Objects;

public class Label {
    private final String value;
    private static int labelCount = 0;

    public static void resetLabelCount() {
        labelCount = 0;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Label label = (Label) o;
        return Objects.equals(value, label.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
