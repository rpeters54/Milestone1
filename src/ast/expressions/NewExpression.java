package ast.expressions;

import ast.*;
import ast.declarations.TypeDeclaration;
import ast.types.*;
import instructions.*;
import instructions.llvm.BitcastLLVMInstruction;
import instructions.llvm.MallocCallLLVMInstruction;

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
      TypeDeclaration decl = env.getTypeDeclaration(id);
      if (decl == null) {
         throw new TypeException(String.format("NewExpression: Can't " +
                 "Alloc Undefined Type, line: %d", getLineNum()));
      }
      return new StructType(-1, decl.getName());
   }

   @Override
   public Source toStackInstructions(BasicBlock block, IrFunction func) {
      return evalMalloc(block, func);
   }

   @Override
   public Source toSSAInstructions(BasicBlock block, IrFunction func) {
      return evalMalloc(block, func);
   }

   private Source evalMalloc(BasicBlock block, IrFunction func) {
      // Create a copy of the type that 'id' refers to
      TypeDeclaration td = func.lookupTypeDeclaration(id);
      Type type = new StructType(-1, td.getName());


      // allocate registers for both results
      Register mallocResult = Register.genTypedLocalRegister(new NullType(), block.getLabel());
      Register castResult = Register.genTypedLocalRegister(type.copy(), block.getLabel());

      // generate both instructions and add them to the block
      MallocCallLLVMInstruction call = new MallocCallLLVMInstruction(mallocResult, td);
      BitcastLLVMInstruction cast = new BitcastLLVMInstruction(castResult, mallocResult);

      block.addCode(call);
      block.addCode(cast);

      // return the result of the last operation
      return castResult;
   }
}
