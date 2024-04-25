package ast.expressions;

import ast.*;
import ast.types.*;
import instructions.*;

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
   public Source genInst(BasicBlock block, LLVMEnvironment env) {
      // Create a copy of the type that 'id' refers to
      TypeDeclaration td = env.lookupTypeDeclaration(id);
      Type type = new StructType(-1, td.getName());

      // generate literal referring to struct size
      Literal size = new Literal(new IntType(), Integer.toString(td.getSize()));
      // allocate registers for both results
      Register mallocResult = new Register(new NullType());
      Register castResult = new Register(type.copy());

      // generate both instructions and add them to the block
      MallocCallInstruction call = new MallocCallInstruction(mallocResult, size);
      BitcastInstruction cast = new BitcastInstruction(castResult, mallocResult);
      block.addCode(call);
      block.addCode(cast);

      // return the result of the last operation
      return castResult;

   }
}
