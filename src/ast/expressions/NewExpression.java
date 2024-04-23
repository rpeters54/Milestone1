package ast.expressions;

import ast.*;
import ast.types.*;

import java.util.ArrayList;
import java.util.List;

public class NewExpression
   extends AbstractExpression
{
   private final String id;

   public NewExpression(int lineNum, String id)
   {
      super(lineNum);
      this.id = id;
   }


   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      Type type = env.lookup(id);
      if (!(type instanceof StructType)) {
         throw new TypeException(String.format("NewExpression: Can't " +
                 "Alloc Non-Struct, line: %d", getLineNum()));
      }
      return type;
   }

   @Override
   public Value genInst(BasicBlock block, LLVMEnvironment env) {
      TypeDeclaration td = env.lookupTypeDeclaration(id);
      Type type = new StructType(-1, td.getName());
      // add malloc for new
      String reg = env.getNextReg();
      Value mallocData = new Value(env, new PointerType(new VoidType()), "@malloc");
      Value sizeData = new Value(env, new IntType(), ""+td.getSize());
      List<Value> argList = new ArrayList<>();
      argList.add(sizeData);
      block.addCode(LLVMPrinter.call(reg, mallocData, argList));
      mallocData.updateValue(reg);
      reg = env.getNextReg();
      block.addCode(LLVMPrinter.bitcast(reg, mallocData, env.typeToString(type)));

      return new Value(env, type, reg);

   }
}
