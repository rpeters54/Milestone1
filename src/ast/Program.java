package ast;

import ast.types.*;
import instructions.*;

import java.util.*;
import java.util.stream.Collectors;

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

    public BasicBlock programCFG() {
        BasicBlock top = new BasicBlock();
        LLVMEnvironment env = new LLVMEnvironment();

        // add driver code to the top of the file
        top.addCode(new DriverDeclarationInstruction());

        // add all type declarations and globals to the environment
        for (TypeDeclaration typeDecl : types) {
            env.addTypeDeclaration(typeDecl.getName(), typeDecl);
            TypeDeclarationInstruction typeDeclInst = new TypeDeclarationInstruction(typeDecl);
            top.addCode(typeDeclInst);
        }

        for (Declaration decl : decls) {
            Register global = new Register(new PointerType(decl.getType()), decl.getName(), true);
            env.addGlobalBinding(decl.getName(), global);
            GlobalRegisterDeclarationInstruction gRDI = new GlobalRegisterDeclarationInstruction(global);
            top.addCode(gRDI);
        }

        for (Function func : funcs) {
            BasicBlock functionBlock = new BasicBlock();
            FunctionStub funcStub = new FunctionStub((FunctionType) func.getType().copy(), func.getName());
            env.addFunctionStub(func.getName(), funcStub);

            List<Type> paramList = func.getParams().stream().map(Declaration::getType).collect(Collectors.toList());

            FunctionDefinitionInstruction funDef = new FunctionDefinitionInstruction(funcStub, paramList);
            functionBlock.addCode(funDef);

            func.genBlock(functionBlock, env);

            top.addChild(functionBlock);
        }

        return top;
    }


}
