package ast;

import ast.statements.Statement;
import ast.types.FunctionType;
import ast.types.PointerType;
import ast.types.Type;
import ast.types.VoidType;
import instructions.*;

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
        List<Declaration> allDecls = new ArrayList<>(params);
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


    public static Label returnLabel;
    public static Register returnReg;

    @Override
    public BasicBlock genBlock(BasicBlock block, LLVMEnvironment env) {
        // reset the register count back to zero
        Register.resetCount();

        List<Declaration> allDecls = new ArrayList<>(params);
        allDecls.addAll(locals);

        // collect all implicitly defined regs
        List<Register> implicitRegs = new ArrayList<>();
        for (Declaration param: params) {
            implicitRegs.add(new Register(param.getType().copy()));
        }

        // one register has to be skipped
        Register skip = new Register();

        // declare a container to hold the return value (helps with cleanup)
        returnReg = new Register(new PointerType(retType.copy()));
        AllocaInstruction returnAlloca = new AllocaInstruction(returnReg);
        block.addCode(returnAlloca);

        //declare a label for returns to jump to
        returnLabel = new Label();

        for (Declaration decl: allDecls) {
            Register localVar = new Register(new PointerType(decl.getType().copy()));
            AllocaInstruction alloca = new AllocaInstruction(localVar);
            block.addCode(alloca);
            env.addLocalBinding(decl.getName(), localVar);
        }
        for (int i = 0; i < params.size(); i++) {
            Declaration param = params.get(i);
            Register implicitReg = implicitRegs.get(i);
            Register localVar = env.lookupReg(param.getName());
            StoreInstruction store = new StoreInstruction(localVar, implicitReg);
            block.addCode(store);
        }

        // add the function epilogue
        BasicBlock endOfBody = body.genBlock(block,env);
        BasicBlock lastBlock = new BasicBlock();
        endOfBody.addChild(lastBlock);

        // if the last statement does not end with a call to return, and a branch to the return statement
        if (!endOfBody.endsWithJump()) {
            UnconditionalBranchInstruction returnBridge = new UnconditionalBranchInstruction(Function.returnLabel);
            endOfBody.addCode(returnBridge);
        }

        //add the return jump label
        lastBlock.addCode(returnLabel);

        if (retType instanceof VoidType) {
            ReturnVoidInstruction retVoid = new ReturnVoidInstruction();
            lastBlock.addCode(retVoid);
        } else {
            Register loadResult = new Register(retType.copy());
            LoadInstruction load = new LoadInstruction(loadResult, returnReg);
            ReturnInstruction ret = new ReturnInstruction(loadResult);

            lastBlock.addCode(load);
            lastBlock.addCode(ret);
        }
        lastBlock.addCode(new EndOfFunction());

        return block;
    }
}
