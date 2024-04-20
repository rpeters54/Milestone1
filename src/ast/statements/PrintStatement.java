package ast.statements;

import ast.TypeEnvironment;
import ast.TypeException;
import ast.expressions.Expression;
import ast.statements.AbstractStatement;
import ast.types.IntType;
import ast.types.Type;

public class PrintStatement
   extends AbstractStatement
{
   private final Expression expression;

   public PrintStatement(int lineNum, Expression expression)
   {
      super(lineNum);
      this.expression = expression;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      Type type = expression.typecheck(env);
      if (!(type instanceof IntType)) {
         throw new TypeException(String.format("PrintStatement: Non-Integer " +
                 "Argument, line: %d", getLineNum()));
      }
      return type;
   }

   @Override
   public boolean alwaysReturns() {
      return false;
   }
}
