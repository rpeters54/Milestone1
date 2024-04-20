package ast.lvalues;

import ast.*;
import ast.expressions.Expression;
import ast.lvalues.Lvalue;
import ast.types.StructType;
import ast.types.Type;

public class LvalueDot implements Lvalue {
    private final int lineNum;
    private final Expression left;
    private final String id;

    public LvalueDot(int lineNum, Expression left, String id) {
        this.lineNum = lineNum;
        this.left = left;
        this.id = id;
    }


    @Override
    public Type typecheck(TypeEnvironment env) throws TypeException {
        Type lType = left.typecheck(env);
        if (!(lType instanceof StructType)) {
            throw new TypeException(String.format("LValueDot: Can't Use Dot " +
                    "Operator On Non-Struct, line: %d", lineNum));
        }
        StructType lSType = (StructType) lType;
        TypeDeclaration lDecl = env.getTypeDeclaration(lSType.getName());
        if (lDecl == null) {
            throw new TypeException(String.format("LValueDot: Couldn't Resolve" +
                    "Left Type, line: %d", lineNum));
        }

        for (Declaration decl : lDecl.getFields()) {
            if (id.equals(decl.getName())) {
                return decl.getType();
            }
        }

        throw new TypeException(String.format("LValueDot: Couldn't Find Field " +
                "Matching 'id', line: %d", lineNum));
    }


    @Override
    public LLVMMetadata genLLVM(BasicBlock block, LLVMEnvironment env) {
        LLVMMetadata structData = left.genLLVM(block, env);
        StructType type = (StructType) structData.getType();
        TypeDeclaration structDecl = env.lookupTypeDeclaration(type.getName());
        int memberIndex = structDecl.locateMember(id);
        Declaration memberDecl = structDecl.getFields().get(memberIndex);
        Type memberType = memberDecl.getType();
        int reg = env.getCurrentRegister();
        block.addCode(LLVMPrinter.sprintGEP(reg, structData, memberIndex));
        return new LLVMMetadata(
                memberType, env.typeToString(memberType), reg);
    }
}
