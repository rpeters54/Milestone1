package ast.expressions;

import ast.*;
import ast.types.NullType;
import ast.types.Type;
import instructions.Literal;
import instructions.Source;

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
   public Source genInst(BasicBlock block, LLVMEnvironment env) {
      return new Literal(new NullType(), "null");
   }
}




