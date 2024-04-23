package ast.expressions;

import ast.*;
import ast.types.IntType;
import ast.types.Type;

public class IntegerExpression
   extends AbstractExpression
{
   private final String value;

   public IntegerExpression(int lineNum, String value)
   {
      super(lineNum);
      this.value = value;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      return new IntType();
   }


   @Override
   public Value genInst(BasicBlock block, LLVMEnvironment env) {
      return new Value(env, new IntType(), value);
   }
}
