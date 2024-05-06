package ast.expressions;

import ast.*;
import ast.types.IntType;
import ast.types.Type;
import instructions.ReadCallInstruction;
import instructions.Register;
import instructions.Source;

public class ReadExpression
   extends AbstractExpression
{
   public ReadExpression(int lineNum)
   {
      super(lineNum);
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      return new IntType();
   }

   @Override
   public Source toStackInstructions(BasicBlock block, IrFunction func) {
      return evalRead(block);
   }

   @Override
   public Source toSSAInstructions(BasicBlock block, IrFunction func) {
      return evalRead(block);
   }

   private Source evalRead(BasicBlock block) {
      Register dummy = Register.genTypedLocalRegister(new IntType(), block.getLabel());
      Register readResult = Register.genTypedLocalRegister(new IntType(), block.getLabel());
      ReadCallInstruction read = new ReadCallInstruction(dummy, readResult);
      block.addCode(read);
      return readResult;
   }
}


