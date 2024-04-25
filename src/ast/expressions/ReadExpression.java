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
   public Source genInst(BasicBlock block, LLVMEnvironment env) {
      Register dummy = new Register(new IntType());
      Register readResult = new Register(new IntType());
      ReadCallInstruction read = new ReadCallInstruction(dummy, readResult);
      block.addCode(read);
      return readResult;
   }
}


