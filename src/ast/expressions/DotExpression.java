package ast.expressions;

import ast.*;
import ast.declarations.Declaration;
import ast.declarations.TypeDeclaration;
import ast.types.*;
import instructions.*;
import instructions.llvm.GetElemPtrLLVMInstruction;
import instructions.llvm.LoadLLVMInstruction;

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
    public Source toStackInstructions(BasicBlock block, IrFunction func) {
        // retrieve type of item
        Source structData = left.toStackInstructions(block, func);
        return evalDot(block, func, structData);
    }

    @Override
    public Source toSSAInstructions(BasicBlock block, IrFunction func) {
        Source structData = left.toSSAInstructions(block, func);
        return evalDot(block, func, structData);
    }

    private Source evalDot(BasicBlock block, IrFunction func, Source structData) {
        StructType type = (StructType) structData.getType();

        // find type declaration from type
        TypeDeclaration structDecl = func.lookupTypeDeclaration(type.getName());

        // find position of member in the struct
        int memberIndex = structDecl.locateMember(id);

        // get the type of the member for the load instruction
        Declaration memberDecl = structDecl.getFields().get(memberIndex);
        Type memberType = memberDecl.getType();

        // create a literal representing the index into the struct
        Literal indexLiteral = new Literal(new IntType(), Integer.toString(memberIndex), block.getLabel());

        // get next 2 regs
        Register gepResult = Register.genMemberRegister(new PointerType(memberType.copy()), block.getLabel());
        Register loadResult = Register.genTypedLocalRegister(memberType.copy(), block.getLabel());

        // format instruction strings
        GetElemPtrLLVMInstruction gep = new GetElemPtrLLVMInstruction(gepResult, structData, indexLiteral);
        LoadLLVMInstruction load = new LoadLLVMInstruction(loadResult, gepResult);

        // add instructions to the basic block
        block.addCode(gep);
        block.addCode(load);

        // return member value
        return loadResult;
    }
}
