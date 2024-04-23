package ast.lvalues;

import ast.*;
import ast.expressions.Expression;
import ast.types.IntType;
import ast.types.PointerType;
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
    public Value genInst(BasicBlock block, LLVMEnvironment env) {
        //get struct metadata
        Value structData = left.genInst(block, env);
        StructType type = (StructType) structData.getType();
        TypeDeclaration structDecl = env.lookupTypeDeclaration(type.getName());
        //find the index of the member in the struct
        int memberIndex = structDecl.locateMember(id);
        Value indexData = new Value(env, new IntType(), ""+memberIndex);
        Declaration memberDecl = structDecl.getFields().get(memberIndex);
        //get the type of the member declaration
        Type memberType = memberDecl.getType();
        String reg = env.getNextReg();
        block.addCode(LLVMPrinter.GEP(reg, structData, indexData));
        //return metadata for pointer to struct member
        return new Value(env, new PointerType(memberType), reg);
    }
}
