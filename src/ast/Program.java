package ast;

import ast.declarations.Declaration;
import ast.declarations.TypeDeclaration;
import ast.types.*;
import instructions.*;
import instructions.llvm.DriverDeclarationLLVMInstruction;
import instructions.llvm.GlobalRegisterDeclarationLLVMInstruction;
import instructions.llvm.TypeDeclarationLLVMInstruction;

import java.util.*;

public class Program {
    private final List<TypeDeclaration> types;
    private final List<Declaration> decls;
    private final List<Function> funcs;


    public Program(List<TypeDeclaration> types,
                   List<Declaration> decls,
                   List<Function> funcs) {
        this.types = types;
        this.decls = decls;
        this.funcs = funcs;
    }

    public boolean validMain() {
        for (Function func : funcs) {
            if (func.getName().equals("main")) {
                return true;
            }
        }
        return false;
    }

    public void validTypes() throws TypeException {

        TypeEnvironment env = new TypeEnvironment();
        /* verify there are no conflicting type declarations */
        /* add them to a list of defined types */
        for (TypeDeclaration td : types) {
            boolean check = env.addTypeDeclaration(td);
            if (!check) {
                throw new TypeException(String.format("Program: " +
                        "Invalid Type Declaration, line %d", td.getLineNum()));
            }
        }

        /* Add all variable declarations to the Type Environment */
        try {
            env.batchExtend(decls);
        } catch (TypeException e) {
            throw new TypeException("Program: " +
                    "Invalid Global Declaration");
        }

        /* Check for duplicate functions */
        List<FunctionType> funcTypes = new ArrayList<>();
        for (Function func : funcs) {
            funcTypes.add(new FunctionType(func));
        }
        Set<FunctionType> set = new HashSet<>(funcTypes);
        if (funcTypes.size() != set.size()) {
            throw new TypeException("Program: Duplicate Function Definitions");
        }

        /* Add all functions declarations to the Type Environment */
        /* Type check each function */
        for (int i = 0; i < funcs.size(); i++) {
            try {
                env.extend(funcs.get(i).getName(), funcTypes.get(i));
            } catch (TypeException e) {
                throw new TypeException(String.format("Program: " +
                        "Invalid Function Type, line %d", funcs.get(i).getLineNum()));
            }
            /* update the current function (helps type check returns) */
            env.setCurrentFunc(new FunctionType(funcs.get(i)));
            funcs.get(i).typecheck(env);

        }
    }

    public boolean validReturns() {
        for (Function func : funcs) {
            if (!(func.getRetType() instanceof VoidType)
                    && !func.getBody().alwaysReturns()) {
                return false;
            }
        }
        return true;
    }

    public IrProgram toCFG(CFGType type) {
        IrProgram prog = new IrProgram();

        // add driver code to the top of the file
        prog.addToHeader(new DriverDeclarationLLVMInstruction());

        // add type declarations to env and program header
        for (TypeDeclaration typeDecl : types) {
            prog.addTypeDeclaration(typeDecl);
            TypeDeclarationLLVMInstruction typeDeclInst = new TypeDeclarationLLVMInstruction(typeDecl);
            prog.addToHeader(typeDeclInst);
        }

        // add all global registers
        for (Declaration decl : decls) {
            Register global = Register.genGlobalRegister(new PointerType(decl.getType()), decl.getName());
            prog.addGlobalBinding(decl.getName(), global);
            GlobalRegisterDeclarationLLVMInstruction gRDI = new GlobalRegisterDeclarationLLVMInstruction(global);
            prog.addToHeader(gRDI);
        }

        // add each function
        switch(type) {
            case STACK -> {
                for (Function func : funcs) {
                    IrFunction processedFunction = func.toStackCFG(prog);
                    prog.addIrFunction(processedFunction);
                }
            }
            case SSA -> {
                for (Function func : funcs) {
                    IrFunction processedFunction = func.toSSACFG(prog);
                    prog.addIrFunction(processedFunction);
                }
            }
        }

        return prog;
    }


    public enum CFGType {
        STACK, SSA
    }
}
