package instructions;

import ast.types.NullType;
import ast.types.Type;

import java.util.Objects;

public class Register implements Source {
    private Type type;
    private String value;
    private Label label;
    private boolean isGlobal;
    private boolean isMember;

    private static int regCount = 0;

    public static void resetCount() {
        regCount = 0;
    }

    public Register(Type type, String val, Label label, boolean isGlobal, boolean isMember) {
        this.type = type;
        this.value = val;
        this.label = label;
        this.isGlobal = isGlobal;
        this.isMember = isMember;
    }

    public static Register genLocalRegister(Label label) {
        return new Register(
                new NullType(),
                "r"+regCount++,
                label,
                false,
                false
        );
    }

    public static Register genTypedLocalRegister(Type type, Label label) {
        return new Register(
                type,
                "r"+regCount++,
                label,
                false,
                false
        );
    }

    public static Register genGlobalRegister(Type type, String name) {
        return new Register(
                type,
                name,
                new Label("global"),
                true,
                false
        );
    }

    public static Register genMemberRegister(Type type, Label label) {
        return new Register(
                type,
                "r"+regCount++,
                label,
                false,
                true
        );
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public Label getLabel() {
        return label;
    }

    public String getName() {
        return value;
    }

    @Override
    public String getValue() {
        if (isGlobal) {
            return "@"+value;
        } else {
            return "%"+value;
        }
    }

    @Override
    public void setLabel(Label label) {
        this.label = label;
    }

    @Override
    public String getTypeString() {
        return TypeMap.ttos(type);
    }

    @Override
    public Source copy() {
        return new Register(type, value, label, isGlobal, isMember);
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Register register = (Register) o;
        return isGlobal == register.isGlobal
                && isMember == register.isMember
                && Objects.equals(label, register.label)
                && Objects.equals(type, register.type)
                && Objects.equals(value, register.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type, label, isGlobal, isMember);
    }
}
