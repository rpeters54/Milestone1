package ast.statements;

import ast.*;
import ast.types.FunctionType;
import ast.types.Type;
import ast.types.VoidType;

public class ReturnEmptyStatement
   extends AbstractStatement
{
   public ReturnEmptyStatement(int lineNum)
   {
      super(lineNum);
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      FunctionType func = env.getCurrentFunc();

      if (func.getOutput() instanceof VoidType) {
         return new VoidType();
      } else {
         throw new TypeException(String.format("ReturnEmptyStatement: Empty Return " +
                 "in Non-Void Function, line: %d", getLineNum()));
      }
   }

   @Override
   public boolean alwaysReturns() {
      return true;
   }

   @Override
   public BasicBlock genBlock(BasicBlock block, LLVMEnvironment env) {
      block.addCode(LLVMPrinter.unCondBranch(env.getRetLabel()));
      return block;
   }
}
