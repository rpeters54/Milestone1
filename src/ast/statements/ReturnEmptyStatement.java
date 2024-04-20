package ast.statements;

import ast.TypeEnvironment;
import ast.TypeException;
import ast.statements.AbstractStatement;
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
         throw new TypeException(String.format("DeleteStatement: Empty Return " +
                 "in Non-Void Function, line: %d", getLineNum()));
      }
   }

   @Override
   public boolean alwaysReturns() {
      return true;
   }
}
