package ast.lvalues;

import ast.*;
import ast.expressions.Expression;
import ast.types.IntType;
import ast.types.PointerType;
import ast.types.StructType;
import ast.types.Type;
import instructions.GetElemPtrInstruction;
import instructions.Literal;
import instructions.Register;
import instructions.Source;

import java.util.List;

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
    public Source genInst(BasicBlock block, LLVMEnvironment env) {
        //get struct metadata
        Source structData = left.genInst(block, env);
        StructType type = (StructType) structData.getType();
        TypeDeclaration structDecl = env.lookupTypeDeclaration(type.getName());

        //find the index of the member in the struct
        int memberIndex = structDecl.locateMember(id);

        //get the type of the member declaration
        Declaration memberDecl = structDecl.getFields().get(memberIndex);
        Type memberType = memberDecl.getType();

        Literal indexLiteral = new Literal(new IntType(), Integer.toString(memberIndex));
        Register gepResult = new Register(new PointerType(memberType.copy()));

        GetElemPtrInstruction gep = new GetElemPtrInstruction(gepResult, structData, indexLiteral);
        block.addCode(gep);

        //return metadata for pointer to struct member
        return gepResult;
    }
}
