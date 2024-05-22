package instructions;

import java.util.Objects;

public class Label {
    private final String name;
    private static int labelCount = 0;

    public static void resetLabelCount() {
        labelCount = 0;
    }

    public Label(String name) {
        this.name = name;
    }

    public Label() {
        this.name = "lab"+labelCount++;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%s:", name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Label label = (Label) o;
        return Objects.equals(name, label.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
