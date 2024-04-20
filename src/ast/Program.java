package ast;

import ast.types.*;

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
        Set<FunctionType> set = new HashSet<FunctionType>(funcTypes);
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

    public BasicBlock programCFG() throws TypeException {
        BasicBlock top = new BasicBlock();
        LLVMEnvironment env = new LLVMEnvironment();

        for (TypeDeclaration typeDecl : types) {
            env.addTypeDeclaration(typeDecl.getName(), typeDecl);
            top.addCode(typeDecl.genGlobal(env));
        }
        for (Declaration decl : decls) {
            top.addCode(decl.genGlobal(env));
        }
        for (Function func : funcs) {
            BasicBlock functionBlock = new BasicBlock();
            func.genLLVM(functionBlock, env);
            top.addChild(functionBlock);
        }

        for (String s : top.getContents()) {
            System.out.println(s);
        }
        for (BasicBlock block : top.getChildren()) {
            for (String s : block.getContents()) {
                System.out.println(s);
            }
        }
        return null;
    }



}
