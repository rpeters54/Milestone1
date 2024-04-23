package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.types.IntType;
import ast.types.Type;

public class PrintLnStatement
   extends AbstractStatement
{
   private final Expression expression;

   public PrintLnStatement(int lineNum, Expression expression)
   {
      super(lineNum);
      this.expression = expression;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      // guard must be a boolean
      Type type = expression.typecheck(env);
      if (!(type instanceof IntType)) {
         throw new TypeException(String.format("PrintLNStatement: Non-Integer Argument, line: %d", getLineNum()));
      }
      return type;
   }

   @Override
   public boolean alwaysReturns() {
      return false;
   }

   @Override
   public BasicBlock genBlock(BasicBlock block, LLVMEnvironment env) {
      Value printItem = expression.genInst(block, env);
      block.addCode(String.format("call i32 (i8*, ...)* @printf(i8* getelementptr" +
              " inbounds ([5 x i8]* @.println, i32 0, i32 0), i64 %s)", printItem.getValue()));
      return block;
   }
}
