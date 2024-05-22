package ast.statements;

import ast.*;
import ast.types.*;
import instructions.llvm.UnconditionalBranchLLVMInstruction;

public class ReturnEmptyStatement
   extends AbstractStatement
{
   public ReturnEmptyStatement(int lineNum)
   {
      super(lineNum);
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      FunctionType func = env.getCurrentFunc();

      if (func.getOutput() instanceof VoidType) {
         return new VoidType();
      } else {
         throw new TypeException(String.format("ReturnEmptyStatement: Empty Return " +
                 "in Non-Void Function, line: %d", getLineNum()));
      }
   }

   @Override
   public boolean alwaysReturns() {
      return true;
   }

   @Override
   public BasicBlock toStackBlocks(BasicBlock block, IrFunction func) {
      UnconditionalBranchLLVMInstruction jump = new UnconditionalBranchLLVMInstruction(Function.returnLabel);
      block.addCode(jump);
      block.addChild(Function.returnBlock);
      return block;
   }

   @Override
   public BasicBlock toSSABlocks(BasicBlock block, IrFunction func) {
      UnconditionalBranchLLVMInstruction jump = new UnconditionalBranchLLVMInstruction(Function.returnLabel);
      block.addCode(jump);
      block.addChild(Function.returnBlock);
      return block;
   }


}
