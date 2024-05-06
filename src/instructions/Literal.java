package instructions;

import ast.types.Type;

import java.util.Objects;

public class Literal implements Source {
    private Type type;
    private String value;
    private Label label;

    public Literal(Type type, String value, Label label) {
        this.type = type;
        this.value = value;
        this.label = label;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Label getLabel() {
        return label;
    }

    @Override
    public String getTypeString() {
        return TypeMap.ttos(type);
    }

    @Override
    public Source copy() {
        return new Literal(type, value, label);
    }

    @Override
    public void setLabel(Label label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Literal literal = (Literal) o;
        return Objects.equals(type, literal.type)
                && Objects.equals(value, literal.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
