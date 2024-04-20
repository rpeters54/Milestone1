package ast;

import java.util.List;

public class LLVMPrinter {

    public static String sprintBinop(int resultReg, String op, LLVMMetadata left, LLVMMetadata right) {
        return String.format("%%%d = %s %s %s, %s",
                resultReg, op, left.getInlineType(), left.getValue(), right.getValue());
    }

    public static String sprintCmp(int resultReg, String op, LLVMMetadata left, LLVMMetadata right) {
        return String.format("%%%d = icmp %s %s %s, %s",
                resultReg, op, left.getInlineType(), left.getValue(), right.getValue());
    }

    public static String sprintLoad(int resultReg, LLVMMetadata loc) {
        return String.format("%%%d = load %s* %s",
                resultReg, loc.getInlineType(), loc.getValue());
    }

    public static String sprintStore(int resultReg, LLVMMetadata item, LLVMMetadata loc) {
        return String.format("%%%d = store %s %s, %s* %s",
                resultReg, item.getInlineType(), item.getValue(),
                loc.getInlineType(), loc.getValue());
    }

    public static String sprintGEP(int resultReg, LLVMMetadata struct, int index) {
        return String.format("%%%d = getelementptr %s* %s, i1 0, i32 %d",
                resultReg, struct.getInlineType(), struct.getValue(), index);
    }

    public static String sprintCall(int resultReg, LLVMMetadata func, List<LLVMMetadata> args) {
        String start = String.format("%%%d = call %s @%s(",
                resultReg, func.getInlineType(), func.getValue());
        StringBuilder callBuilder = new StringBuilder(start);
        int startLength = callBuilder.length();
        for (LLVMMetadata arg : args) {
            callBuilder.append(String.format("%s %s, ",
                    arg.getInlineType(), arg.getValue()));
        }
        if (startLength != callBuilder.length()) {
            callBuilder.delete(callBuilder.length()-2, callBuilder.length());
        }
        callBuilder.append(")");
        return callBuilder.toString();
    }

    public static String sprintReturn(LLVMMetadata retVal) {
        return String.format("ret %s %s",
                retVal.getInlineType(), retVal.getValue());
    }

    public static String sprintReturnVoid() {
        return "ret void";
    }
}
