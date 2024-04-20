package parser;

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

        try {
            program.programCFG();
        } catch (ast.TypeException e) {
            e.printStackTrace();
        }

    }


    private static String _inputFile = null;

    private static void parseParameters(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].charAt(0) == '-') {
                System.err.println("unexpected option: " + args[i]);
                exit(1);
            } else if (_inputFile != null) {
                System.err.println("too many files specified");
                exit(1);
            } else {
                _inputFile = args[i];
            }
        }
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
