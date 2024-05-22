package ast.expressions;

import ast.*;
import ast.types.IntType;
import ast.types.Type;
import ast.types.TypeEnvironment;
import ast.types.TypeException;
import instructions.llvm.ReadCallLLVMInstruction;
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
      return evalRead(block, func);
   }

   @Override
   public Source toSSAInstructions(BasicBlock block, IrFunction func) {
      return evalRead(block, func);
   }

   private Source evalRead(BasicBlock block, IrFunction func) {
      Register dummy = Register.genTypedLocalRegister(new IntType(), block.getLabel());
      Register readResult = Register.genTypedLocalRegister(new IntType(), block.getLabel());
      ReadCallLLVMInstruction read = new ReadCallLLVMInstruction(dummy, readResult);
      block.addCode(read);

      return readResult;
   }
}


