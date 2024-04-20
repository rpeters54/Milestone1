package ast.types;

import ast.Declaration;
import ast.Function;

import java.util.ArrayList;
import java.util.Objects;

public class FunctionType implements Type {

    private ArrayList<Type> inputs;
    private Type output;

    public FunctionType(Function func) {
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
        return Objects.equals(inputs, that.inputs) && Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputs, output);
    }

}
