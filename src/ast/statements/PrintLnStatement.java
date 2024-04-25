package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.types.IntType;
import ast.types.Type;
import instructions.PrintCallInstruction;
import instructions.Register;
import instructions.Source;

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
      Source printItem = expression.genInst(block, env);
      Register dummy = new Register(new IntType());
      PrintCallInstruction print = new PrintCallInstruction(dummy, printItem, true);
      block.addCode(print);
      return block;
   }
}
