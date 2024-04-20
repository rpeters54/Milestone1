package ast.statements;


import ast.TypeEnvironment;
import ast.TypeException;
import ast.expressions.Expression;
import ast.statements.AbstractStatement;
import ast.types.Type;

public class InvocationStatement
   extends AbstractStatement
{
   private final Expression expression;

   public InvocationStatement(int lineNum, Expression expression)
   {
      super(lineNum);
      this.expression = expression;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      return expression.typecheck(env);
   }

   @Override
   public boolean alwaysReturns() {
      return false;
   }
}
