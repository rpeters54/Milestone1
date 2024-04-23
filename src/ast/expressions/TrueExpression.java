package ast.expressions;

import ast.*;
import ast.types.BoolType;
import ast.types.Type;

public class TrueExpression
   extends AbstractExpression
{
   public TrueExpression(int lineNum)
   {
      super(lineNum);
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      return new BoolType();
   }


   @Override
   public Value genInst(BasicBlock block, LLVMEnvironment env) {
      return new Value(env, new BoolType(), "true");
   }
}


