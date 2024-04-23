package ast;

import java.util.List;

public class LLVMPrinter {

    /* return formatted binary expression llvm instruction */
    public static String binop(String result, String op, Value left, Value right) {
        return String.format("%s = %s %s %s, %s",
                result, op, left.getIrType(), left.getValue(), right.getValue());
    }

    /* return formatted cmp llvm instruction */
    public static String icmp(String result, String op, Value left, Value right) {
        return String.format("%s = icmp %s %s %s, %s",
                result, op, left.getIrType(), left.getValue(), right.getValue());
    }

    public static String condBranch(Value cond, String ifTrue, String ifFalse) {
        return String.format("br i1 %s, label %%%s, label %%%s",
                cond.getValue(), ifTrue, ifFalse);
    }

    public static String unCondBranch(String dest) {
        return String.format("br label %%%s", dest);
    }

    /* return formatted load llvm instruction */
    public static String load(String result, Value loc) {
        return String.format("%s = load %s, %s",
                result, loc.getIrType(), loc.getValue());
    }

    /* return formatted store llvm instruction */
    public static String store(Value item, Value loc) {
        return String.format("store %s %s, %s %s",
                item.getIrType(), item.getValue(),
                loc.getIrType(), loc.getValue());
    }

    /* return formatted getelementptr llvm instruction (used for pointer arith) */
    public static String GEP(String result, Value struct, Value index) {
        return String.format("%s = getelementptr %s %s, i1 0, %s %s",
                result, struct.getIrType(), struct.getValue(),
                index.getIrType(), index.getValue());
    }

    /* return formatted functional call llvm instruction */
    public static String call(String result, Value func, List<Value> args) {
        String start;
        if (result == null) {
            start = String.format("call %s %s(",
                    func.getIrType(), func.getValue());
        } else {
            start = String.format("%s = call %s %s(",
                    result, func.getIrType(), func.getValue());
        }

        StringBuilder callBuilder = new StringBuilder(start);
        int startLength = callBuilder.length();
        for (Value arg : args) {
            callBuilder.append(String.format("%s %s, ",
                    arg.getIrType(), arg.getValue()));
        }
        if (startLength != callBuilder.length()) {
            callBuilder.delete(callBuilder.length()-2, callBuilder.length());
        }
        callBuilder.append(")");
        return callBuilder.toString();
    }

    public static String funDef(String irRetType, String name, List<Value> params) {
        String stubStart = String.format("define %s %s(", irRetType, name);
        StringBuilder stubBuilder = new StringBuilder(stubStart);
        int before = stubBuilder.length();
        for (Value param : params) {
            stubBuilder.append(String.format("%s, ", param.getIrType()));
        }
        if (before != stubBuilder.length()) {
            stubBuilder.delete(stubBuilder.length() - 2, stubBuilder.length());
        }
        stubBuilder.append(") {");
        return stubBuilder.toString();
    }

    /* return formatted return statement */
    public static String returns(Value retVal) {
        return String.format("ret %s %s",
                retVal.getIrType(), retVal.getValue());
    }

    public static String returnsVoid() {
        return "ret void";
    }

    public static String global(String result, String inlineType) {
        return String.format("%s = common global %s", result, inlineType);
    }

    public static String alloca(String result, String inlineType) {
        return String.format("%s = alloca %s", result, inlineType);
    }

    public static String bitcast(String result, Value uncasted, String resultType) {
        return String.format("%s = bitcast %s %s to %s", result, uncasted.getIrType(),
                uncasted.getValue(), resultType);
    }

    public static String label(String label) {
        return String.format("%s:", label);
    }
}
