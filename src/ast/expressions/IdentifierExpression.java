package ast.expressions;

import ast.*;
import ast.types.PointerType;
import ast.types.Type;
import instructions.LoadInstruction;
import instructions.Register;
import instructions.Source;

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
   public Source genInst(BasicBlock block, LLVMEnvironment env) {
      Register loadResult = new Register();
      Register idReg = env.lookupReg(id);

      PointerType ptr = (PointerType) idReg.getType();
      loadResult.setType(ptr.getBaseType().copy());

      LoadInstruction load = new LoadInstruction(loadResult, idReg);
      block.addCode(load);

      return loadResult;
   }
}
