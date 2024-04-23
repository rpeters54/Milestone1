package ast;

import ast.statements.Statement;
import ast.types.FunctionType;
import ast.types.PointerType;
import ast.types.Type;
import ast.types.VoidType;

import java.util.*;

public class Function implements Typed, BlockHandler {
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


    public static Value retVal;

    @Override
    public BasicBlock genBlock(BasicBlock block,
                                LLVMEnvironment env) {
        env.refreshLocals();

        // skip all implicitly defined regs
        List<String> implicitRegs = new ArrayList<>();
        for (Declaration param: params) {
            implicitRegs.add(env.getNextReg());
        }

        // declare a container to hold the return value (helps with cleanup)
        String reg = env.getNextReg();
        Value retVal = new Value(env, retType, reg);
        block.addCode(LLVMPrinter.alloca(retVal.getValue(), retVal.getIrType()));
        retVal.updateType(env, new PointerType(retType));
        Function.retVal = retVal;

        for (Declaration param: params) {
            reg = env.getNextReg();
            block.addCode(LLVMPrinter.alloca(reg, env.typeToString(param.getType())));
            env.addLocalBinding(param.getName(), param.getType(), reg);
        }
        for (int i = 0; i < params.size(); i++) {
            Declaration param = params.get(i);
            Value item = new Value(env, param.getType(), implicitRegs.get(i));
            Value loc = new Value(env, new PointerType(param.getType()), env.lookupRegBinding(param.getName()));
            block.addCode(LLVMPrinter.store(item, loc));
        }
        for (Declaration decl : locals) {
            reg = env.getNextReg();
            env.addLocalBinding(decl.getName(), decl.getType(), reg);
            block.addCode(LLVMPrinter.alloca(reg, env.typeToString(decl.getType())));
        }
        BasicBlock lastBlock = body.genBlock(block,env);
        lastBlock.addCode(LLVMPrinter.label(env.getRetLabel()));

        if (retType instanceof VoidType) {
            lastBlock.addCode(LLVMPrinter.returnsVoid());
        } else {
            reg = env.getNextReg();
            lastBlock.addCode(LLVMPrinter.load(reg, Function.retVal));
            Value lastReg = new Value(env, retType, reg);
            lastBlock.addCode(LLVMPrinter.returns(lastReg));
        }
        lastBlock.addCode("}");
        return block;
    }
}
