package ast.statements;


import ast.*;
import ast.expressions.Expression;
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

   @Override
   public BasicBlock toStackBlocks(BasicBlock block, IrFunction func) {
      expression.toStackInstructions(block, func);
      return block;
   }

   @Override
   public BasicBlock toSSABlocks(BasicBlock block, IrFunction func) {
      expression.toSSAInstructions(block, func);
      return block;
   }
}
