package ast.types;

import ast.Declaration;
import ast.Function;

import java.util.ArrayList;
import java.util.Objects;

public class FunctionType implements Type {

    private final String name;
    private final ArrayList<Type> inputs;
    private final Type output;

    public FunctionType(Function func) {
        name = func.getName();

        inputs = new ArrayList<>(func.getParams().size());

        for (Declaration param : func.getParams()) {
            inputs.add(param.getType());
        }

        output = func.getRetType();
    }

    public ArrayList<Type> getInputs() {
        return inputs;
    }

    public Type getOutput() {
        return output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionType that = (FunctionType) o;
        return Objects.equals(inputs, that.inputs) && Objects.equals(output, that.output)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, inputs, output);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public Type copy() {
        try {
            return (Type) clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
