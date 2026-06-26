import rules.Expr;
import rules.Stmt;
import token.Token;
import token.TokenType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Stmt.Visitor<Void>, Expr.Visitor<Void> {

    private enum FunctionType {
        NONE, FUNCTION
    }

    private enum ContextType {
        NONE, LOOP, SWITCH
    }

    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private final Map<Expr, Integer> locals = new HashMap<>();

    private FunctionType currentFunction = FunctionType.NONE;
    private int loopDepth = 0;
    private int switchDepth = 0;

    public Map<Expr, Integer> resolve(List<Stmt> statements) {
        for (Stmt stmt : statements) {
            resolve(stmt);
        }
        return locals;
    }

    private void resolveExprList(List<Expr> expressions) {
        for (Expr expr : expressions) {
            resolve(expr);
        }
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    @Override
    public Void visitPrefix(Expr.Prefix expr) {
        resolve(expr.right());
        return null;
    }

    @Override
    public Void visitPostfix(Expr.Postfix expr) {
        resolve(expr.left());
        return null;
    }

    @Override
    public Void visitBinary(Expr.Binary expr) {
        resolve(expr.left());
        resolve(expr.right());
        return null;
    }

    @Override
    public Void visitTernary(Expr.Ternary expr) {
        resolve(expr.condition());
        resolve(expr.thenBranch());
        resolve(expr.elseBranch());
        return null;
    }

    @Override
    public Void visitAssign(Expr.Assign expr) {
        resolve(expr.value());

        Token var = expr.var();
        if (var.token() == TokenType.IDENTIFIER) {
            String name = (String) var.value();
            resolveLocal(expr, name);
        } else {
            error("Invalid assignment target.");
        }

        return null;
    }

    @Override
    public Void visitLogical(Expr.Logical expr) {
        resolve(expr.left());
        resolve(expr.right());
        return null;
    }

    @Override
    public Void visitGrouping(Expr.Grouping expr) {
        resolve(expr.expression());
        return null;
    }

    @Override
    public Void visitLiteral(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitIdentifier(Expr.Identifier expr) {
        String name = (String) expr.name().value();
        if (!scopes.isEmpty() && scopes.peek().get(name) == Boolean.FALSE) {
            error("Cannot read local variable '" + name + "' in its own initializer.");
        }

        resolveLocal(expr, name);
        return null;
    }

    @Override
    public Void visitStringConcat(Expr.StringConcat expr) {
        for (Expr str : expr.strings()) {
            resolve(str);
        }
        return null;
    }

    @Override
    public Void visitCall(Expr.Call expr) {
        resolve(expr.callee());
        for (Expr argument : expr.arguments()) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitArrayIndex(Expr.ArrayIndex expr) {
        resolve(expr.expr());
        return null;
    }

    @Override
    public Void visitEcho(Stmt.Echo stmt) {
        resolve(stmt.expr());
        return null;
    }

    @Override
    public Void visitInput(Expr.Input stmt) {
        // no nothing
        return null;
    }

    @Override
    public Void visitTypeCast(Expr.TypeCast expr) {
        resolve(expr.expr());
        return null;
    }

    @Override
    public Void visitError(Stmt.Error stmt) {
        resolve(stmt.expr());
        return null;
    }

    @Override
    public Void visitVarDecl(Stmt.VarDecl stmt) {

        // declare the variable name in the stack
        String name = (String) stmt.name().value();
        declare(name);

        // define (initialize) the variable if defined
        if (stmt.init() != null) {
            resolve(stmt.init());
        }

        define(name);
        return null;
    }

    @Override
    public Void visitExprStmt(Stmt.ExprStmt stmt) {
        resolve(stmt.expr());
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.IfStmt stmt) {
        resolve(stmt.condition());
        resolve(stmt.thenBranch());
        if (stmt.elseBranch() != null) {
            resolve(stmt.elseBranch());
        }
        return null;
    }

    @Override
    public Void visitSwitchStmt(Stmt.SwitchStmt stmt) {

        switchDepth++;

        resolve(stmt.condition());
        for (Stmt caseStmt : stmt.cases()) {
            resolve(caseStmt);
        }

        switchDepth--;
        return null;
    }

    @Override
    public Void visitCaseBlock(Stmt.CaseBlock stmt) {
        resolve(stmt.caseExpr());
        resolve(stmt.body());
        return null;
    }

    @Override
    public Void visitDefaultBlock(Stmt.DefaultBlock stmt) {
        resolve(stmt.body());
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.WhileStmt stmt) {
        loopDepth++;

        resolve(stmt.condition());
        resolve(stmt.content());

        loopDepth--;
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.ForStmt stmt) {
        beginScope();

        loopDepth++;

        resolve(stmt.init());
        resolve(stmt.condition());
        resolveExprList(stmt.incrementer());

        resolve(stmt.content());

        endScope();

        loopDepth--;
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.ReturnStmt stmt) {
        if (currentFunction == FunctionType.NONE) {
            error("Cannot return from top-level code.");
        }

        if (stmt.value() != null) {
            resolve(stmt.value());
        }
        return null;
    }

    @Override
    public Void visitBlock(Stmt.Block blockStmt) {
        beginScope();
        resolve(blockStmt.statements());
        endScope();
        return null;
    }

    @Override
    public Void visitFunction(Stmt.Function stmt) {
        String name = (String) stmt.name().value();
        declare(name);
        define(name);

        FunctionType enclosingFunction = currentFunction;
        currentFunction = FunctionType.FUNCTION;

        int enclosingLoopDepth = loopDepth;
        int enclosingSwitchDepth = switchDepth;

        loopDepth = 0;
        switchDepth = 0;

        beginScope();

        for (Stmt.Parameter param : stmt.params()) {
            String pName = (String) param.name().value();
            declare(pName);
            define(pName);
        }

        resolve(stmt.body());

        endScope();

        currentFunction = enclosingFunction;
        loopDepth = enclosingLoopDepth;
        switchDepth = enclosingSwitchDepth;
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.BreakStmt stmt) {
        if (loopDepth == 0 && switchDepth == 0) {
            error("Cannot 'break' outside of a loop or switch statement.");
        }
        return null;
    }

    @Override
    public Void visitContinueStmt(Stmt.ContinueStmt stmt) {
        if (loopDepth == 0) {
            error("Cannot 'continue' outside of a loop statement.");
        }
        return null;
    }

    // helper methods

    // search for variable
    private void resolveLocal(Expr expr, String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name)) {
                locals.put(expr, scopes.size() - 1 - i);
                return;
            }
        }
        // not found, assume it's a global variable
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(String name) {
        // skip if variable is declared globally
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name)) {
            error("Identifier '" + name + "' has already been declared");
            return;
        }
        scopes.peek().put(name, false);
    }

    private void define(String name) {
        // skip global variables
        if (scopes.isEmpty()) return;

        // set variable to defined
        scopes.peek().put(name, true);
    }

    private void error(String message) {
        System.err.println(message);
        Main.hadError = true;
    }
}
