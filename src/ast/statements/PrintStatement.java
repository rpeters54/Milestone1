package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.types.IntType;
import ast.types.Type;
import instructions.PrintCallInstruction;
import instructions.Register;
import instructions.Source;

public class PrintStatement
   extends AbstractStatement
{
   private final Expression expression;

   public PrintStatement(int lineNum, Expression expression)
   {
      super(lineNum);
      this.expression = expression;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      Type type = expression.typecheck(env);
      if (!(type instanceof IntType)) {
         throw new TypeException(String.format("PrintStatement: Non-Integer " +
                 "Argument, line: %d", getLineNum()));
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
      PrintCallInstruction print = new PrintCallInstruction(dummy, printItem, false);
      block.addCode(print);
      return block;
   }
}
