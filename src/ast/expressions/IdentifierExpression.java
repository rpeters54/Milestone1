package ast.expressions;

import ast.*;
import ast.types.Type;

public class IdentifierExpression
   extends AbstractExpression
{
   private final String id;

   public IdentifierExpression(int lineNum, String id)
   {
      super(lineNum);
      this.id = id;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      return env.lookup(id);
   }

   @Override
   public Value genInst(BasicBlock block, LLVMEnvironment env) {
      String reg = env.getNextReg();
      Value idData = new Value(env, env.lookupTypeBinding(id), env.lookupRegBinding(id));
      // print load value to register
      block.addCode(LLVMPrinter.load(reg, idData));
      // update value object so that the location is the register loaded to
      idData.updateValue(reg);
      return idData;
   }
}
