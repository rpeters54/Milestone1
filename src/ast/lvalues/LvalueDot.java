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

public class LvalueDot implements Lvalue {
    private final int lineNum;
    private final Expression left;
    private final String id;

    public LvalueDot(int lineNum, Expression left, String id) {
        this.lineNum = lineNum;
        this.left = left;
        this.id = id;
    }

    public String getId() {
        return id;
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
    public Source toStackInstructions(BasicBlock block, IrFunction func) {
        //get struct metadata
        Source structData = left.toStackInstructions(block, func);
        return evalLvalDot(block, func, structData);
    }

    @Override
    public Source toSSAInstructions(BasicBlock block, IrFunction func) {
        //get struct metadata
        Source structData = left.toSSAInstructions(block, func);
        return evalLvalDot(block, func, structData);
    }

    public Source evalLvalDot(BasicBlock block, IrFunction func, Source structData) {
        StructType type = (StructType) structData.getType();
        TypeDeclaration structDecl = func.lookupTypeDeclaration(type.getName());

        //find the index of the member in the struct
        int memberIndex = structDecl.locateMember(id);

        //get the type of the member declaration
        Declaration memberDecl = structDecl.getFields().get(memberIndex);
        Type memberType = memberDecl.getType();

        Literal indexLiteral = new Literal(new IntType(), Integer.toString(memberIndex), block.getLabel());
        Register gepResult = Register.genMemberRegister(new PointerType(memberType.copy()), block.getLabel());

        GetElemPtrInstruction gep = new GetElemPtrInstruction(gepResult, structData, indexLiteral);
        block.addCode(gep);

        //return metadata for pointer to struct member
        return gepResult;
    }
}
