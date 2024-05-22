package ast.expressions;

import ast.*;
import ast.types.BoolType;
import ast.types.Type;
import ast.types.TypeEnvironment;
import ast.types.TypeException;
import instructions.Literal;
import instructions.Source;

public class FalseExpression
   extends AbstractExpression
{
   public FalseExpression(int lineNum)
   {
      super(lineNum);
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      return new BoolType();
   }

   @Override
   public Source toStackInstructions(BasicBlock block, IrFunction func) {
      return new Literal(new BoolType(), "false", block.getLabel());
   }

   @Override
   public Source toSSAInstructions(BasicBlock block, IrFunction func) {
      return new Literal(new BoolType(), "false", block.getLabel());
   }
}
