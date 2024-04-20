package ast.expressions;

import ast.*;
import ast.expressions.AbstractExpression;
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
   public LLVMMetadata genLLVM(BasicBlock block, LLVMEnvironment env) {
      int reg = env.getCurrentRegister();
      Type type = env.lookupBinding(id);
      String typeSpecifier = env.typeToString(type);
      block.addCode(String.format("%%%d = load %s, %s* %s",
              reg, typeSpecifier, typeSpecifier, id));
      return new LLVMMetadata(type, typeSpecifier, reg);
   }
}
