package ast;

import ast.statements.Statement;
import ast.types.FunctionType;
import ast.types.Type;

import java.util.*;

public class Function implements Typed, Codegen {
    private final int lineNum;
    private final String name;
    private final Type retType;
    private final List<Declaration> params;
    private final List<Declaration> locals;
    private final Statement body;

    public Function(int lineNum, String name, List<Declaration> params,
                    Type retType, List<Declaration> locals, Statement body) {
        this.lineNum = lineNum;
        this.name = name;
        this.params = params;
        this.retType = retType;
        this.locals = locals;
        this.body = body;
    }

    public int getLineNum() {
        return lineNum;
    }

    public String getName() {
        return name;
    }

    public List<Declaration> getParams() {
        return params;
    }

    public Type getRetType() {
        return retType;
    }

    public Type getType() {
        return new FunctionType(this);
    }

    public Statement getBody() {
        return body;
    }

    public List<Declaration> concatDecls() {
        List<Declaration> allDecls = new ArrayList<Declaration>(params);
        allDecls.addAll(locals);
        return allDecls;
    }

    @Override
    public Type typecheck(TypeEnvironment env) throws TypeException {
        /* add all defined locals and params to the type environment */
        List<Declaration> allDecls = concatDecls();
        try {
            env.batchExtend(allDecls);
        } catch (TypeException e) {
            throw new TypeException(String.format("Function: Failed To Extend " +
                    "Environment, line: %d", lineNum));
        }

        /* type check each statement in the function */
        Type type = body.typecheck(env);

        /* remove items added to the type environment */
        env.batchRemove(allDecls.size());

        /* return the type of the last evaluated statement */
        return type;
    }

    @Override
    public LLVMMetadata genLLVM(BasicBlock block,
                          LLVMEnvironment env) {
        List<Declaration> allDecls = concatDecls();
        LLVMEnvironment copy = env.copy();
        block.addCode(formatFunctionStub(env));
        for (Declaration decl : allDecls) {
            copy.addBinding(decl.getName(), decl.getType());
            block.addCode(decl.genLocal(env));
        }
        return null;
    }

    public String formatFunctionStub(LLVMEnvironment env) {
        String stubStart = String.format("define %s @%s(",
                env.typeToString(retType), name);
        StringBuilder stubBuilder = new StringBuilder(stubStart);
        int before = stubBuilder.length();
        for (Declaration param : params) {
            stubBuilder.append(String.format("%s %s, ",
                    env.typeToString(param.getType()), param.getName()));
        }
        if (before != stubBuilder.length()) {
            stubBuilder.delete(stubBuilder.length() - 2, stubBuilder.length());
        }
        stubBuilder.append(")");
        return stubBuilder.toString();
    }
}
