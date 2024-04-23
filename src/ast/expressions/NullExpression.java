package ast.expressions;

import ast.*;
import ast.types.NullType;
import ast.types.Type;

public class NullExpression extends AbstractExpression {
   public NullExpression(int lineNum)
   {
      super(lineNum);
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      return new NullType();
   }

   @Override
   public Value genInst(BasicBlock block, LLVMEnvironment env) {
      return new Value(env, new NullType(), "null");
   }
}




