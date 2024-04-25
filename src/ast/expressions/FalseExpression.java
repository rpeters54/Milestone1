package ast.expressions;

import ast.*;
import ast.types.BoolType;
import ast.types.Type;
import instructions.Literal;
import instructions.Source;

public class FalseExpression
   extends AbstractExpression
{
   public FalseExpression(int lineNum)
   {
      super(lineNum);
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      return new BoolType();
   }

   @Override
   public Source genInst(BasicBlock block, LLVMEnvironment env) {
      return new Literal(new BoolType(), "false");
   }
}
