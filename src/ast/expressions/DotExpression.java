package ast.expressions;

import ast.*;
import ast.types.IntType;
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
    public Value genInst(BasicBlock block, LLVMEnvironment env) {
        // retrieve type of item
        Value structData = left.genInst(block, env);
        StructType type = (StructType) structData.getType();

        // find type declaration from type
        TypeDeclaration structDecl = env.lookupTypeDeclaration(type.getName());

        // find position of member in the struct
        int memberIndex = structDecl.locateMember(id);

        // turn the index into a value
        Value indexData = new Value(env, new IntType(),""+memberIndex);

        // get next 2 regs
        String reg = env.getNextReg();
        String reg2 = env.getNextReg();

        // format instruction strings
        String gep = String.format("%s = getelementptr %s %s, i1 0, %s %s",
                reg, structData.getIrType(), structData.getValue(),
                indexData.getIrType(), indexData.getValue());

        // get the type of the member for the load instruction
        Declaration memberDecl = structDecl.getFields().get(memberIndex);
        Type memberType = memberDecl.getType();

        String load = String.format("%s = load %s, %s",
                reg2, env.typeToString(memberType), reg);

        // add instructions to the basic block
        block.addCode(gep);
        block.addCode(load);

        // return member value
        return new Value(env, memberType, reg2);
    }
}
