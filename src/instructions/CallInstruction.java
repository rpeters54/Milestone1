package instructions;

import java.util.List;

public class CallInstruction implements Instruction {
    private final Register result;
    private final FunctionStub function;
    private final List<Source> arguments;

    public CallInstruction(Register result, FunctionStub function, List<Source> arguments) {
        this.result = result;
        this.function = function;
        this.arguments = arguments;
    }

    public CallInstruction(FunctionStub function, List<Source> arguments) {
        this.result = null;
        this.function = function;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        String start;
        if (result == null) {
            start = String.format("call %s %s(", function.getTypeString(), function.getValue());
        } else {
            start = String.format("%s = call %s %s(", result.getValue(), function.getTypeString(), function.getValue());
        }
        StringBuilder callBuilder = new StringBuilder(start);
        int startLength = callBuilder.length();
        List<String> argTypes = function.getArgTypeString();
        for (int i = 0; i < argTypes.size(); i++) {
            callBuilder.append(String.format("%s %s, ", argTypes.get(i), arguments.get(i).getValue()));
        }
        if (startLength != callBuilder.length()) {
            callBuilder.delete(callBuilder.length()-2, callBuilder.length());
        }
        callBuilder.append(")");
        return callBuilder.toString();
    }
}
