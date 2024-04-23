package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.types.FunctionType;
import ast.types.PointerType;
import ast.types.Type;

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
   public BasicBlock genBlock(BasicBlock block, LLVMEnvironment env) {
      Value val = expression.genInst(block, env);
      block.addCode(LLVMPrinter.store(val, Function.retVal));
      block.addCode(LLVMPrinter.unCondBranch(env.getRetLabel()));
      return block;
   }
}
