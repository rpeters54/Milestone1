package instructions;

import ast.Declaration;
import ast.types.FunctionType;
import ast.Function;

import java.util.ArrayList;
import java.util.List;


public class CallInstruction implements Instruction {
    private Register result;
    private final Function function;
    private final List<Source> arguments;

    public CallInstruction(Register result, Function function, List<Source> arguments) {
        this.result = result;
        this.function = function;
        this.arguments = arguments;
    }

    public CallInstruction(Function function, List<Source> arguments) {
        this.result = null;
        this.function = function;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        String start;
        FunctionType type = new FunctionType(function);
        if (result == null) {
            start = String.format("call %s @%s(", TypeMap.ttos(type.getOutput()), function.getName());
        } else {
            start = String.format("%s = call %s @%s(", result.getValue(), TypeMap.ttos(type.getOutput()), function.getName());
        }
        StringBuilder callBuilder = new StringBuilder(start);
        int startLength = callBuilder.length();
        List<String> argTypes = new ArrayList<>();
        for (Declaration param : function.getParams()) {
            argTypes.add(TypeMap.ttos(param.getType()));
        }
        for (int i = 0; i < argTypes.size(); i++) {
            callBuilder.append(String.format("%s %s, ", argTypes.get(i), arguments.get(i).getValue()));
        }
        if (startLength != callBuilder.length()) {
            callBuilder.delete(callBuilder.length()-2, callBuilder.length());
        }
        callBuilder.append(")");
        return callBuilder.toString();
    }

    @Override
    public void substitute(Source item, Source replacement) {
        if (item.equals(result)) {
            if (replacement instanceof Register) {
                replacement.setLabel(result.getLabel());
                result = (Register) replacement;
            }
            throw new RuntimeException("CallInstruction: Tried to replace necessary Register with Source");
        }
        for (int i = 0; i < arguments.size(); i++) {
            if (arguments.get(i).equals(item)) {
                replacement.setLabel(arguments.get(i).getLabel());
                arguments.set(i, replacement);
            }
        }
    }
}
