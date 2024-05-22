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
   public BasicBlock toStackBlocks(BasicBlock block, IrFunction func) {
      Source printItem = expression.toStackInstructions(block, func);
      return evalPrint(block, printItem);
   }

   @Override
   public BasicBlock toSSABlocks(BasicBlock block, IrFunction func) {
      Source printItem = expression.toSSAInstructions(block, func);
      return evalPrint(block, printItem);
   }

   public BasicBlock evalPrint(BasicBlock block, Source printItem) {
      Register dummy = Register.genTypedLocalRegister(new IntType(), block.getLabel());
      PrintCallLLVMInstruction print = new PrintCallLLVMInstruction(dummy, printItem, false);
      block.addCode(print);
      return block;
   }
}
