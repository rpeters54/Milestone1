package ast.lvalues;

import ast.TypeEnvironment;
import ast.TypeException;
import ast.expressions.Expression;
import ast.lvalues.Lvalue;
import ast.types.ArrayType;
import ast.types.IntType;
import ast.types.Type;

public class LvalueIndex implements Lvalue {
    private final int lineNum;
    private final Expression left;
    private final Expression index;

    public LvalueIndex(int lineNum, Expression left, Expression index) {
        this.lineNum = lineNum;
        this.left = left;
        this.index = index;
    }

    @Override
    public Type typecheck(TypeEnvironment env) throws TypeException {
        Type lType = left.typecheck(env);
        Type rType = index.typecheck(env);
        if (!(lType instanceof ArrayType)) {
            throw new TypeException(String.format("LValueIndex: Tried to " +
                    "Index into Non-Array, line %d", lineNum));
        }
        if (!(rType instanceof IntType)) {
            throw new TypeException(String.format("LValueIndex: Can't Have " +
                    "Non-Int Index, line %d", lineNum));
        }
        return new IntType();
    }
}
