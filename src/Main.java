import rules.Expr;
import rules.Stmt;
import token.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static boolean hadError = false;

    public static void main(String[] args) {

        if (args.length == 1) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nProgram interrupted.");
            }));

            run(args[0]);
        } else {
            System.exit(64);
        }
    }

    public static void run(String sourceCode) {
        (new Main()).compile(sourceCode);
    }

    public void compile(String path) {
        File file = new File(path);
        try (Lexer lexer = new Lexer(file)) {
            List<Token> tokens = tokenize(lexer);
            if (hadError) return;

            List<Stmt> statements = parse(tokens);
            if (hadError) return;

            Map<Expr, Integer> locals = resolve(statements);
            if (hadError) return;

            evaluate(statements, locals);
        }
        catch (FileNotFoundException e) {
            System.err.println("File not found in Main.openFile(String) " + path);
            e.printStackTrace();
        }
        catch (IOException e) {
            System.err.println("IOException in Main.openFile(String)");
            e.printStackTrace();
        }
    }

    private void evaluate(List<Stmt> statements, Map<Expr, Integer> locals) {
        Interpreter interpreter = new Interpreter(locals);
        interpreter.interpret(statements);
    }

    private Map<Expr, Integer> resolve(List<Stmt> statements) {
        Resolver resolver = new Resolver();
        return resolver.resolve(statements);
    }

    private List<Token> tokenize(Lexer lexer) throws IOException {
        List<Token> tokens = lexer.tokenize();
        for (Token t : tokens) {
            System.out.println(t);
        }
        return tokens;
    }

    private List<Stmt> parse(List<Token> tokens) {
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        for (Stmt t : statements) {
            System.out.println(t);
        }
        return statements;
    }

    public static final void error(int line, String message) {
        System.err.println("[Line: " + line + "] Error: " + message);
        hadError = true;
    }
}