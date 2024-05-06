package ast.expressions;

import ast.*;
import ast.types.NullType;
import ast.types.Type;
import instructions.Literal;
import instructions.Source;

public class NullExpression extends AbstractExpression {
   public NullExpression(int lineNum)
   {
      super(lineNum);
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      return new NullType();
   }

   @Override
   public Source toStackInstructions(BasicBlock block, IrFunction func) {
      return new Literal(new NullType(), "null", block.getLabel());
   }

   @Override
   public Source toSSAInstructions(BasicBlock block, IrFunction func) {
      return new Literal(new NullType(), "null", block.getLabel());
   }
}




