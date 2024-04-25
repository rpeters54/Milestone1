package instructions;

import ast.types.Type;

import java.util.List;

public class FunctionDefinitionInstruction implements Instruction {
    private final FunctionStub function;
    private final List<Type> paramList;

    public FunctionDefinitionInstruction(FunctionStub function, List<Type> paramList) {
        this.function = function;
        this.paramList = paramList;
    }

    @Override
    public String toString() {
        String stubStart = String.format("define %s %s(", function.getTypeString(), function.getValue());
        StringBuilder stubBuilder = new StringBuilder(stubStart);
        int before = stubBuilder.length();
        for (Type param : paramList) {
            stubBuilder.append(String.format("%s, ", TypeMap.ttos(param)));
        }
        if (before != stubBuilder.length()) {
            stubBuilder.delete(stubBuilder.length() - 2, stubBuilder.length());
        }
        stubBuilder.append(") {");
        return stubBuilder.toString();
    }
}
