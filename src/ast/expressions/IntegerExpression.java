package ast.expressions;

import ast.*;
import ast.types.IntType;
import ast.types.Type;
import instructions.Literal;
import instructions.Source;

public class IntegerExpression
   extends AbstractExpression
{
   private final String value;

   public IntegerExpression(int lineNum, String value)
   {
      super(lineNum);
      this.value = value;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      return new IntType();
   }


   @Override
   public Source toStackInstructions(BasicBlock block, IrFunction func) {
      return new Literal(new IntType(), value, block.getLabel());
   }

   @Override
   public Source toSSAInstructions(BasicBlock block, IrFunction func) {
      return new Literal(new IntType(), value, block.getLabel());
   }
}
