package instructions.llvm;

import ast.declarations.Declaration;
import ast.types.FunctionType;
import ast.Function;
import instructions.Instruction;
import instructions.Register;
import instructions.Source;
import instructions.arm.ArmInstruction;
import instructions.arm.BranchLinkArmInstruction;
import instructions.arm.MovArmInstruction;

import java.util.ArrayList;
import java.util.List;


public class CallLLVMInstruction extends AbstractLLVMInstruction implements CriticalInstruction {
    private final Function function;

    public CallLLVMInstruction(Register result, Function function, List<Source> arguments) {
        super(result, arguments);
        this.function = function;
    }

    public CallLLVMInstruction(Function function, List<Source> arguments) {
        super(null, arguments);
        this.function = function;
    }

    @Override
    public String toString() {
        String start;
        FunctionType type = new FunctionType(function);
        if (getResult() == null) {
            start = String.format("call %s @%s(", TypeMap.ttos(type.getOutput()), function.getName());
        } else {
            start = String.format("%s = call %s @%s(", getResult().llvmString(), TypeMap.ttos(type.getOutput()), function.getName());
        }
        StringBuilder callBuilder = new StringBuilder(start);
        int startLength = callBuilder.length();
        List<String> argTypes = new ArrayList<>();
        for (Declaration param : function.getParams()) {
            argTypes.add(TypeMap.ttos(param.getType()));
        }
        for (int i = 0; i < argTypes.size(); i++) {
            callBuilder.append(String.format("%s %s, ", argTypes.get(i), getSource(i).llvmString()));
        }
        if (startLength != callBuilder.length()) {
            callBuilder.delete(callBuilder.length()-2, callBuilder.length());
        }
        callBuilder.append(")");
        return callBuilder.toString();
    }

    @Override
    public List<Instruction> toArm() {
        List<Instruction> instList = new ArrayList<>();
        if (getSources().size() > 8) {
            throw new RuntimeException("CallLLVMInstruction - toArm: Can't Define a function with > 8 args (Sorry)");
        }
        for (int i = 0; i < getSources().size(); i++) {
            instList.add(new MovArmInstruction(Register.genArmRegister(i), getSource(i)));
        }
        instList.add(new BranchLinkArmInstruction(function.getName(), function.getParams().size()));
        if (getResult() != null) {
            instList.add(new MovArmInstruction(getResult(), Register.genArmRegister(0)));
        }
        return instList;
    }
}
