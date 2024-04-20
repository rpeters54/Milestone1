package ast.statements;

import ast.TypeEnvironment;
import ast.TypeException;
import ast.expressions.Expression;
import ast.statements.AbstractStatement;
import ast.statements.Statement;
import ast.types.BoolType;
import ast.types.Type;
import ast.types.VoidType;

public class WhileStatement
   extends AbstractStatement
{
   private final Expression guard;
   private final Statement body;

   public WhileStatement(int lineNum, Expression guard, Statement body)
   {
      super(lineNum);
      this.guard = guard;
      this.body = body;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      // guard must be a boolean
      if (!(guard.typecheck(env) instanceof BoolType)) {
         throw new TypeException(String.format("WhileStatement: Non-Boolean " +
                 "Guard, line: %d", getLineNum()));
      }

      body.typecheck(env);

      return new VoidType();
   }

   @Override
   public boolean alwaysReturns() {
      return false;
   }

}
