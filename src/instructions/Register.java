package instructions;

import ast.types.NullType;
import ast.types.Type;
import instructions.llvm.TypeMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Register implements Source {
    private Type type;
    private String name;
    private Label label;
    private boolean isGlobal;
    private boolean isMember;

    private static int regCount = 0;

    public static void resetCount() {
        regCount = 0;
    }

    public Register(Type type, String name, Label label, boolean isGlobal, boolean isMember) {
        this.type = type;
        this.name = name;
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

    public static Register genArmRegister(int reg) {
        return new Register(
                null,
                "x"+reg,
                null,
                false,
                false
        );
    }

    public static List<Register> genArmRegisterList(List<Integer> arr) {
        List<Register> regs = new ArrayList<>();
        for (int reg : arr) {
            regs.add(genArmRegister(reg));
        }
        return regs;
    }

    public static List<Register> genArmCallRegisterList() {
        List<Integer> regs = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15);
        return genArmRegisterList(regs);
    }

    public static List<Register> genArmArgRegisterList(int numArgs) {
        List<Integer> regs = IntStream.rangeClosed(0, numArgs)
                .boxed().collect(Collectors.toList());
        if (regs.size() > 8)
            throw new RuntimeException("Compiler Can't handle function calls with greater than 8 arguments... Sorry");
        return genArmRegisterList(regs);
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
        return name;
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
        return new Register(type, name, label, isGlobal, isMember);
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    @Override
    public String toString() {
        if (isGlobal) {
            return "@"+ name;
        } else {
            return "%"+ name;
        }
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
                && Objects.equals(name, register.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, label, isGlobal, isMember);
    }
}
