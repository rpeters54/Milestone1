package ast.expressions;

import ast.*;
import ast.types.PointerType;
import ast.types.Type;
import ast.types.TypeEnvironment;
import ast.types.TypeException;
import instructions.llvm.LoadLLVMInstruction;
import instructions.llvm.PhiLLVMInstruction;
import instructions.Register;
import instructions.Source;

import java.util.List;

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
   public Source toStackInstructions(BasicBlock block, IrFunction func) {
      Register loadResult = Register.genLocalRegister(block.getLabel());
      Register idReg = func.lookupReg(id);

      PointerType ptr = (PointerType) idReg.getType();
      loadResult.setType(ptr.getBaseType().copy());

      LoadLLVMInstruction load = new LoadLLVMInstruction(loadResult, idReg);
      block.addCode(load);

      return loadResult;
   }

   @Override
   public Source toSSAInstructions(BasicBlock block, IrFunction func) {

      Source binding = block.lookupLocalBinding(id);
      // if the binding is already defined, just return it
      if (binding != null) {
         return binding;
      }

      //if not locally bound, it must be a global
      if (!func.isBound(id)) {
         Register loadResult = Register.genLocalRegister(block.getLabel());
         Register idReg = func.lookupGlobal(id);

         PointerType ptr = (PointerType) idReg.getType();
         loadResult.setType(ptr.getBaseType().copy());

         LoadLLVMInstruction load = new LoadLLVMInstruction(loadResult, idReg);
         block.addCode(load);

         return loadResult;
      }


      // if not a global, search the predecessors for all definitions
      List<PhiTuple> bindings = block.searchPredecessors(id);
      // make the phi

      Register phiResult = Register.genTypedLocalRegister(bindings.get(0).getType().copy(), block.getLabel());
      PhiLLVMInstruction phi = new PhiLLVMInstruction(id, phiResult, bindings);
      block.addLocalBinding(id, phiResult);
      block.addCode(phi);

      return phiResult;

   }

}
