package parser;

import ast.BasicBlock;
import ast.IrProgram;
import ast.Program;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.*;

import static java.lang.System.exit;

public class MiniCompiler {
    public static void main(String[] args) throws IOException {
        parseParameters(args);
        CommonTokenStream tokens = new CommonTokenStream(createLexer());
        MiniParser parser = new MiniParser(tokens);
        ParseTree tree = parser.program();

        // return if there is a syntax error
        if (parser.getNumberOfSyntaxErrors() != 0) {
            error("Syntax Error");
        }

        // construct the AST
        MiniToAstProgramVisitor programVisitor = new MiniToAstProgramVisitor();
        ast.Program program = programVisitor.visit(tree);

        // check for main
        if (!program.validMain()) {
            error("Main Function Not Defined");
        }

        // type check
        try {
            program.validTypes();
        } catch (ast.TypeException e) {
            e.printStackTrace();
        }

        // validate returns
        if (!program.validReturns()) {
            error("Invalid Return Path");
        }

        IrProgram prog = program.toCFG(Program.CFGType.SSA);
        prog.toLLFile("binary.ll");
    }


    private static String _inputFile = null;

    private static void parseParameters(String[] args) {
        if (args.length < 1) {
            error("too few args");
        }
        _inputFile = args[0];
    }

    private static void error(String msg) {
        System.err.println(msg);
        exit(-1);
    }

    private static MiniLexer createLexer() {
        try {
            CharStream input;
            if (_inputFile == null) {
                input = CharStreams.fromStream(System.in);
            } else {
                input = CharStreams.fromFileName(_inputFile);
            }
            return new MiniLexer(input);
        } catch (java.io.IOException e) {
            System.err.println("file not found: " + _inputFile);
            exit(1);
            return null;
        }
    }
}
