package ast.statements;

import ast.TypeEnvironment;
import ast.TypeException;
import ast.expressions.Expression;
import ast.statements.AbstractStatement;
import ast.types.StructType;
import ast.types.Type;

public class DeleteStatement
   extends AbstractStatement
{
   private final Expression expression;

   public DeleteStatement(int lineNum, Expression expression)
   {
      super(lineNum);
      this.expression = expression;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      /* if the expression is a struct type that has been declared return it */
      Type type = expression.typecheck(env);
      if (type instanceof StructType) {
         return type;
      }
      throw new TypeException(String.format("DeleteStatement: Invalid " +
              "Structure Type, line: %d", getLineNum()));
   }

   @Override
   public boolean alwaysReturns() {
      return false;
   }
}
