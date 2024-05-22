package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.types.IntType;
import ast.types.Type;
import ast.types.TypeEnvironment;
import ast.types.TypeException;
import instructions.llvm.PrintCallLLVMInstruction;
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
   public BasicBlock toStackBlocks(BasicBlock block, IrFunction func) {
      Source printItem = expression.toStackInstructions(block, func);
      return evalPrintLn(block, printItem);
   }

   @Override
   public BasicBlock toSSABlocks(BasicBlock block, IrFunction func) {
      Source printItem = expression.toSSAInstructions(block, func);
      return evalPrintLn(block, printItem);
   }

   public BasicBlock evalPrintLn(BasicBlock block, Source printItem) {
      Register dummy = Register.genTypedLocalRegister(new IntType(), block.getLabel());
      PrintCallLLVMInstruction print = new PrintCallLLVMInstruction(dummy, printItem, true);
      block.addCode(print);
      return block;
   }
}
