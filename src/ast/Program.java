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

    public BasicBlock programCFG() {
        BasicBlock top = new BasicBlock();
        LLVMEnvironment env = new LLVMEnvironment();

        // add driver code to the top of the file
        // addDrivers(top);

        // add all type declarations and globals to the environment
        for (TypeDeclaration typeDecl : types) {
            env.addTypeDeclaration(typeDecl.getName(), typeDecl);
            top.addCode(typeDecl.genGlobal(env));
        }
        for (Declaration decl : decls) {
            env.addGlobalBinding(decl.getName(), decl.getType(), decl.getName());
            top.addCode(LLVMPrinter.global(
                    env.lookupRegBinding(decl.getName()), env.typeToString(decl.getType())));
        }
        for (Function func : funcs) {
            BasicBlock functionBlock = new BasicBlock();
            env.addGlobalBinding(func.getName(), func.getType(), func.getName());
            List<Value> paramList = new ArrayList<>();
            for (Declaration param : func.getParams()) {
                paramList.add(new Value(env, param.getType(), param.getName()));
            }
            functionBlock.addCode(LLVMPrinter.funDef(env.typeToString(func.getRetType()),
                    env.lookupRegBinding(func.getName()),
                    paramList));
            func.genBlock(functionBlock, env);
            top.addChild(functionBlock);
        }

        return top;
    }

    /*
    void addDrivers(BasicBlock block) {
        block.addCode("declare i8* @malloc(i64)");
        block.addCode("declare void @free(i8*)");
        block.addCode("declare i64 @printf(i8*, ...)");
        block.addCode("declare i64 @scanf(i8*, ...)");
        block.addCode("@.println = private unnamed_addr constant [5 x i8] c\"%ld\\0A\\00\", align 1");
        block.addCode("@.print = private unnamed_addr constant [5 x i8] c\"%ld \\00\", align 1");
        block.addCode("@.read = private unnamed_addr constant [4 x i8] c\"%ld\\00\", align 1");
        block.addCode("@.read_scratch = common global i64 0, align 8");
    }
    */


}
