package ast.expressions;

import ast.*;
import ast.types.StructType;
import ast.types.Type;

public class DotExpression
        extends AbstractExpression {
    private final Expression left;
    private final String id;

    public DotExpression(int lineNum, Expression left, String id) {
        super(lineNum);
        this.left = left;
        this.id = id;
    }


    @Override
    public Type typecheck(TypeEnvironment env) throws TypeException {
        Type lType = left.typecheck(env);
        if (!(lType instanceof StructType)) {
            throw new TypeException(String.format("DotExpression: Can't Use Dot " +
                    "Operator On Non-Struct, line: %d", getLineNum()));
        }
        StructType lSType = (StructType) lType;
        TypeDeclaration lDecl = env.getTypeDeclaration(lSType.getName());
        if (lDecl == null) {
            throw new TypeException(String.format("DotExpression: Couldn't Resolve" +
                    "Left Type, line: %d", getLineNum()));
        }

        for (Declaration decl : lDecl.getFields()) {
            if (id.equals(decl.getName())) {
                return decl.getType();
            }
        }

        throw new TypeException(String.format("DotExpression: Couldn't Find Field " +
                "Matching 'id', line: %d", getLineNum()));
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
        reg = env.getCurrentRegister();
        LLVMMetadata memberData = new LLVMMetadata(
                memberType, env.typeToString(memberType), reg);
        block.addCode(LLVMPrinter.sprintLoad(reg, memberData));
        return memberData;
    }
}
