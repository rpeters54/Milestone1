package ast.expressions;

import ast.*;
import ast.expressions.AbstractExpression;
import ast.types.IntType;
import ast.types.Type;

public class ReadExpression
   extends AbstractExpression
{
   public ReadExpression(int lineNum)
   {
      super(lineNum);
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      return new IntType();
   }

   @Override
   public LLVMMetadata genLLVM(BasicBlock block, LLVMEnvironment env) {
      block.addCode("Read Expression Function Call");

      return null;
   }
}


