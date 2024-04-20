package ast.expressions;

import ast.*;
import ast.expressions.AbstractExpression;
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
   public LLVMMetadata genLLVM(BasicBlock block, LLVMEnvironment env) {
      Type type = new BoolType();
      return new LLVMMetadata(type, env.typeToString(type), "true");
   }
}


