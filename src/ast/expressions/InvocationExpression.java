package ast.expressions;

import ast.*;
import ast.types.FunctionType;
import ast.types.NullType;
import ast.types.StructType;
import ast.types.Type;
import instructions.CallInstruction;
import instructions.FunctionStub;
import instructions.Register;
import instructions.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InvocationExpression
   extends AbstractExpression
{
   private final String name;
   private final List<Expression> arguments;

   public InvocationExpression(int lineNum, String name,
      List<Expression> arguments)
   {
      super(lineNum);
      this.name = name;
      this.arguments = arguments;
   }

   @Override
   public Type typecheck(TypeEnvironment env) throws TypeException {
      // look for function name in the environment
      Type type = env.lookup(name);
      if (!(type instanceof FunctionType)) {
         throw new TypeException(String.format("InvocationExpression: " +
                 "Can't Apply Non-Function, line: %d", getLineNum()));
      }
      FunctionType funcType = (FunctionType) type;

      // Collect the types of all arguments
      List<Type> paramTypes = new ArrayList<>(arguments.size());
      for (Expression arg : arguments) {
         paramTypes.add(arg.typecheck(env));
      }

      // Ensure that the types of the parameters match the function type
      if (paramTypes.size() != funcType.getInputs().size()) {
         throw new TypeException(String.format("InvocationExpression: " +
                 "Number of Parameters Does Not Match Definition, line: %d", getLineNum()));
      }
      for (int i = 0; i < paramTypes.size(); i++) {
         if (!paramTypes.get(i).equals(funcType.getInputs().get(i)) &&
                 !(funcType.getInputs().get(i) instanceof StructType && paramTypes.get(i) instanceof NullType)) {
            throw new TypeException(String.format("InvocationExpression: " +
                    "Parameter Types Do Not Match, line: %d", getLineNum()));
         }
      }

      // return the output type if they match (doesn't matter as long as not EmptyType)
      return funcType.getOutput();
   }

   @Override
   public Source genInst(BasicBlock block, LLVMEnvironment env) {

      // handle list of arguments
      List<Source> argList = arguments.stream().map(arg -> arg.genInst(block, env)).collect(Collectors.toList());

      // retrieve function
      FunctionStub func = env.lookupFunction(name);
      FunctionType funcType = (FunctionType) func.getType();
      // allocate a reg to hold the result
      Register callResult = new Register(funcType.getOutput().copy());

      CallInstruction call = new CallInstruction(callResult, func, argList);
      block.addCode(call);

      return callResult;
   }
}
