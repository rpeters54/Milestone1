package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.types.BoolType;
import ast.types.Type;
import ast.types.VoidType;

public class ConditionalStatement
   extends AbstractStatement
{
   private final Expression guard;
   private final Statement thenBlock;
   private final Statement elseBlock;

   public ConditionalStatement(int lineNum, Expression guard,
      Statement thenBlock, Statement elseBlock)
   {
      super(lineNum);
      this.guard = guard;
      this.thenBlock = thenBlock;
      this.elseBlock = elseBlock;
   }


   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      /* guard must be a boolean */
      if (!(guard.typecheck(env) instanceof BoolType)) {
         throw new TypeException(String.format("ConditionalStatement: Non-Boolean " +
                 "Guard, line: %d", getLineNum()));
      }

      /* ensure both cases properly type check */
      thenBlock.typecheck(env);
      if (elseBlock != null) {
         elseBlock.typecheck(env);
      }

      /* return void on success */
      return new VoidType();
   }

   @Override
   public boolean alwaysReturns() {
      if (elseBlock != null) {
         return thenBlock.alwaysReturns() && elseBlock.alwaysReturns();
      }
      return thenBlock.alwaysReturns();
   }

   @Override
   public BasicBlock genBlock(BasicBlock block, LLVMEnvironment env) {
      // evaluate the guard
      Value guardData = guard.genInst(block, env);
      // create labels for the true basic block and end basic block
      String endLabel = env.getNextBranch();
      String trueLabel = env.getNextBranch();
      // create the end basic block and add its label
      BasicBlock endBlock = new BasicBlock();
      endBlock.addCode(LLVMPrinter.label(endLabel));
      // create the true basic block and add its label
      BasicBlock trueBlock = new BasicBlock();
      trueBlock.addCode(LLVMPrinter.label(trueLabel));
      // add the true block to the list of the first block's children
      block.addChild(trueBlock);
      // evaluate the internals of the true block, possibly generating more blocks
      // keep track of the last block made
      BasicBlock lastTrueBlock = thenBlock.genBlock(trueBlock, env);
      // add a branch to the end block at the end of the final block of the true case
      lastTrueBlock.addCode(LLVMPrinter.unCondBranch(endLabel));
      lastTrueBlock.addChild(endBlock);
      // repeat the same for else if the case exists
      if (elseBlock != null) {
         BasicBlock falseBlock = new BasicBlock();
         String falseLabel = env.getNextBranch();
         falseBlock.addCode(LLVMPrinter.label(falseLabel));
         block.addChild(falseBlock);
         BasicBlock lastFalseBlock = thenBlock.genBlock(falseBlock, env);
         lastFalseBlock.addCode(LLVMPrinter.unCondBranch(endLabel));
         lastFalseBlock.addChild(endBlock);

         // print the conditional branch at the end of the original block
         block.addCode(LLVMPrinter.condBranch(guardData, trueLabel, falseLabel));
      } else {
         // with no else block, false branches to the end block instead
         block.addCode(LLVMPrinter.condBranch(guardData, trueLabel, endLabel));
      }

      return endBlock;
   }
}
