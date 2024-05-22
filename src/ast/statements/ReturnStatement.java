package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.types.FunctionType;
import ast.types.Type;
import ast.types.TypeEnvironment;
import ast.types.TypeException;
import instructions.Register;
import instructions.Source;
import instructions.llvm.StoreLLVMInstruction;
import instructions.llvm.UnconditionalBranchLLVMInstruction;

public class ReturnStatement
   extends AbstractStatement
{
   private final Expression expression;

   public ReturnStatement(int lineNum, Expression expression)
   {
      super(lineNum);
      this.expression = expression;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      /* check if the return statement and function return type match */
      Type retType = expression.typecheck(env);
      FunctionType func = env.getCurrentFunc();

      /* return the return type if they do */
      /* Otherwise return empty type */
      if (func.getOutput().equals(retType)) {
         return retType;
      } else {
         throw new TypeException(String.format("ReturnStatement: Return Type Doesn't " +
                 "Match Function Signature, line: %d", getLineNum()));
      }
   }

   @Override
   public boolean alwaysReturns() {
      return true;
   }


   @Override
   public BasicBlock toStackBlocks(BasicBlock block, IrFunction func) {
      Source val = expression.toStackInstructions(block, func);
      Register retVal = Function.returnReg;

      StoreLLVMInstruction store = new StoreLLVMInstruction(val, retVal);
      UnconditionalBranchLLVMInstruction jump = new UnconditionalBranchLLVMInstruction(Function.returnLabel);

      block.addCode(store);
      block.addCode(jump);
      block.addChild(Function.returnBlock);

      return block;
   }

   @Override
   public BasicBlock toSSABlocks(BasicBlock block, IrFunction func) {
      Source retVal = expression.toSSAInstructions(block, func);
      Function.returnPhi.addMember(new PhiTuple(retVal, block.getLabel()));

      UnconditionalBranchLLVMInstruction jump = new UnconditionalBranchLLVMInstruction(Function.returnLabel);
      block.addCode(jump);
      block.addChild(Function.returnBlock);

      return block;
   }


}
