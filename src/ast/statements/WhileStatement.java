package ast.statements;

import ast.*;
import ast.expressions.Expression;
import ast.expressions.IntegerExpression;
import ast.types.BoolType;
import ast.types.Type;
import ast.types.VoidType;
import instructions.*;

import java.util.*;

public class WhileStatement
   extends AbstractStatement
{
   private final Expression guard;
   private final Statement body;

   public WhileStatement(int lineNum, Expression guard, Statement body)
   {
      super(lineNum);
      this.guard = guard;
      this.body = body;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      // guard must be a boolean
      if (!(guard.typecheck(env) instanceof BoolType)) {
         throw new TypeException(String.format("WhileStatement: Non-Boolean " +
                 "Guard, line: %d", getLineNum()));
      }

      body.typecheck(env);

      return new VoidType();
   }

   @Override
   public boolean alwaysReturns() {
      return false;
   }

   @Override
   public BasicBlock toStackBlocks(BasicBlock block, IrFunction func) {
      // generate code for the guard
      Source guardData = guard.toStackInstructions(block, func);

      // generate labels and blocks
      Label innerStub = new Label();
      BasicBlock inner = new BasicBlock();
      func.addToQueue(inner);

      // add the body and after blocks as children of the parent
      block.addChild(inner);

      // add label to the inner block
      inner.setLabel(innerStub);

      // evaluate the body blocks
      BasicBlock lastInner = body.toStackBlocks(inner, func);

      // add the outermost block
      Label outerStub = new Label();
      BasicBlock outer = new BasicBlock();
      func.addToQueue(outer);

      // add the conditional branch to the inner and outer blocks to the parent
      ConditionalBranchInstruction cond = new ConditionalBranchInstruction(guardData, innerStub, outerStub);
      block.addCode(cond);
      block.addChild(outer);

      //if the body ends with a return/jump to somewhere else,
      // dont put a branch to the outer at the end
      if (!lastInner.endsWithJump()) {
         Source innerGuard = guard.toStackInstructions(lastInner, func);
         ConditionalBranchInstruction innerCond = new ConditionalBranchInstruction(innerGuard, innerStub, outerStub);
         lastInner.addCode(innerCond);
         lastInner.addChild(outer);
      }

      // add the label to the outer stub
      outer.setLabel(outerStub);
      return outer;
   }

   @Override
   public BasicBlock toSSABlocks(BasicBlock block, IrFunction func) {
      // generate code for the guard
      Source guardData = guard.toSSAInstructions(block, func);

      // generate labels and blocks
      Label innerStub = new Label();
      BasicBlock inner = new BasicBlock();
      inner.unseal();
      func.addToQueue(inner);

      // add the body and after blocks as children of the parent
      block.addChild(inner);

      // add label to the inner block
      inner.setLabel(innerStub);


      // generate temporary phis since inner is unsealed
      generateTemporaryPhis(inner, block);


      // evaluate the body blocks
      BasicBlock lastInner = body.toSSABlocks(inner, func);
      lastInner.addChild(inner);

      // update all the phis to be sealed
      Queue<Instruction> code = inner.getContents();
      int size = code.size();
      for (int i = 0; i < size; i++) {
         Instruction inst = code.poll();
         if (inst instanceof PhiInstruction) {
            PhiInstruction phi = (PhiInstruction) inst;
            List<PhiTuple> sources = inner.searchPredecessors(phi.getBoundName());
            phi.setMembers(sources);
         }
         code.add(inst);
      }
      inner.seal();


      // add the outermost block
      Label outerStub = new Label();
      BasicBlock outer = new BasicBlock();
      // add the label to the outer stub
      outer.setLabel(outerStub);
      func.addToQueue(outer);

      // add the conditional branch to the inner and outer blocks to the parent
      ConditionalBranchInstruction cond = new ConditionalBranchInstruction(guardData, innerStub, outerStub);
      block.addCode(cond);
      block.addChild(outer);

      //if the body ends with a return/jump to somewhere else,
      // dont put a branch to the outer at the end
      // only populate outer with the parent's bindings
      if (!lastInner.endsWithJump()) {
         Source innerGuard = guard.toSSAInstructions(lastInner, func);
         ConditionalBranchInstruction innerCond = new ConditionalBranchInstruction(innerGuard, innerStub, outerStub);
         lastInner.addCode(innerCond);
         lastInner.addChild(outer);
         outer.reconcileBranch(block, inner);
      }

      return outer;
   }


   private void generateTemporaryPhis(BasicBlock inner, BasicBlock parent) {
      Map<String, Source> parentBindings = parent.getLocalBindings();
      for (String key : parentBindings.keySet()) {
         Source item = parentBindings.get(key);
         Register phiReg = Register.genTypedLocalRegister(item.getType(), inner.getLabel());
         PhiInstruction phi = new PhiInstruction(key, phiReg, null);
         inner.addLocalBinding(key, phiReg);
         inner.addCode(phi);
      }
   }


}
