import errors.RuntimeError;
import rules.Expr;
import rules.Stmt;
import token.Keyword;
import token.Operator;
import token.Token;
import token.TokenType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class Interpreter implements Stmt.Visitor<Void>, Expr.Visitor<Object> {

    Environment environment;
    Environment globals;
    private final Map<Expr, Integer> locals;

    private Scanner sc = new Scanner(System.in);

    /**
     * 0: NONE
     * 1: BREAK
     * 2: CONTINUE
     */
    private int controlSign = 0;

    public Interpreter(Map<Expr, Integer> locals) {
        this.locals = locals;
        this.globals = new Environment();
        this.environment = this.globals;
    }

    public void interpret(List<Stmt> statements) {
        for (int i = 0; i < statements.size(); i++) {
            execute(statements.get(i));
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void checkNumberOperands(Operator op, Object left, Object right) {
        if (left instanceof Number && right instanceof Number) return;
        throw new RuntimeError("Operands for operator '" + op + "' must be numbers.\nLeft: " + left + ")\nRight: " + right + " (" + right.getClass() + ")");
    }

    private void checkNumberOperand(Operator op, Object value) {
        if (value instanceof Number n) return;
        throw new RuntimeError("Operand for operator '" + op + "' must be a number.");
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        if (a instanceof Number aNum && b instanceof Number bNum) {
            return isNumberEqual(aNum, bNum);
        }
        return a.equals(b);
    }

    private boolean isNumberEqual(Number a, Number b) {
        // compare instances
        if (a == b) return true;

        // check nullity
        if (a == null || b == null) return false;

        BigDecimal bda = toBigDecimal(a);
        BigDecimal bdb = toBigDecimal(b);

        return bda.compareTo(bdb) == 0;
    }

    private BigDecimal toBigDecimal(Number number) {
        if (number instanceof BigDecimal bd) return bd;
        if (number instanceof BigInteger bi) return new BigDecimal(bi);
        if (number instanceof Double || number instanceof Float) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.valueOf(number.longValue());
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Number n) return n.doubleValue() != 0.0;
        if (value instanceof Boolean b) return b;
        if (value instanceof Character ch) return ch != '\0';
        return true;
    }

    private boolean isFloatingPoint(Number n) {
        return n instanceof Double || n instanceof Float;
    }

    private boolean isIntegerOperation(Number l, Number r) {
        return !(isFloatingPoint(l) || isFloatingPoint(r));
    }

    private Object lookUpVariable(String name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.lookup(name, distance);
        } else {
            return globals.lookup(name, 0);
        }
    }

    private void assignVariable(Expr expr, String name, Object value) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assign(name, value, distance);
        } else {
            globals.assign(name, value, 0);
        }
    }

    @Override
    public Object visitPrefix(Expr.Prefix expr) {
        Object right = evaluate(expr.right());

        return switch (expr.op()) {
            case DOUBLE_MINUS -> {
                if (!(expr.right() instanceof Expr.Identifier id)) {
                    throw new RuntimeError("Invalid decrement target.");
                }
                checkNumberOperand(expr.op(), right);
                Object newValue = (right instanceof Double || right instanceof Float)
                        ? ((Number) right).doubleValue() - 1.0
                        : ((Number) right).longValue() - 1;
                assignVariable(id, (String) id.name().value(), newValue);
                yield newValue;
            }
            case DOUBLE_PLUS -> {
                if (!(expr.right() instanceof Expr.Identifier id)) {
                    throw new RuntimeError("Invalid decrement target.");
                }
                checkNumberOperand(expr.op(), right);
                Object newValue = (right instanceof Double || right instanceof Float)
                        ? ((Number) right).doubleValue() + 1.0
                        : ((Number) right).longValue() + 1;
                assignVariable(id, (String) id.name().value(), newValue);
                yield newValue;
            }
            case EXCLAMATION -> !isTruthy(right);
            case MINUS -> {
                if (right instanceof Number n) {
                    if (isFloatingPoint(n)) {
                        yield -n.doubleValue();
                    }
                    yield -n.longValue();
                }
                throw new RuntimeError("Negated value must be a number.");
            }
            default -> throw new RuntimeError("Unsupported prefix operator.");
        };
    }

    @Override
    public Object visitPostfix(Expr.Postfix expr) {

        switch (expr.op()) {
            case DOUBLE_PLUS -> {
                if (!(expr.left() instanceof Expr.Identifier id)) {
                    throw new RuntimeError("Invalid increment target.");
                }
                String name = (String) id.name().value();
                Object oldValue = lookUpVariable(name, id);
                checkNumberOperand(expr.op(), oldValue);

                Number num = (Number) oldValue;
                Object newValue = isFloatingPoint(num) ? num.doubleValue() - 1.0 : num.longValue() - 1;

                assignVariable(id, name, newValue);
                return oldValue;
            }
            case DOUBLE_MINUS -> {
                if (!(expr.left() instanceof Expr.Identifier id)) {
                    throw new RuntimeError("Invalid decrement target.");
                }

                String name = (String) id.name().value();
                Object oldValue = lookUpVariable(name, id);
                checkNumberOperand(expr.op(), oldValue);

                Number num = (Number) oldValue;
                Object newValue = isFloatingPoint(num) ? num.doubleValue() : num.longValue();

                assignVariable(id, name, newValue);
                return oldValue;
            }
        }
        throw new RuntimeError("Unsupported postfix operator.");
    }

    @Override
    public Object visitBinary(Expr.Binary expr) {
        Object left = evaluate(expr.left());
        Object right = evaluate(expr.right());

        return switch (expr.op()) {
            case PLUS -> {
                checkNumberOperands(expr.op(), left, right);
                Number l = (Number) left;
                Number r = (Number) right;

                if (isIntegerOperation(l, r)) {
                    yield l.longValue() + r.longValue();
                }
                yield l.doubleValue() + r.doubleValue();
            }
            case MINUS -> {
                checkNumberOperands(expr.op(), left, right);
                Number l = (Number) left;
                Number r = (Number) right;

                if (isIntegerOperation(l, r)) {
                    yield l.longValue() - r.longValue();
                }
                yield l.doubleValue() - r.doubleValue();
            }
            case STAR -> {
                checkNumberOperands(expr.op(), left, right);
                Number l = (Number) left;
                Number r = (Number) right;

                if (isIntegerOperation(l, r)) {
                    yield l.longValue() * r.longValue();
                }
                yield l.doubleValue() * r.doubleValue();
            }
            case SLASH -> {
                checkNumberOperands(expr.op(), left, right);
                // Optional: check for division by zero if you don't want Double's Infinity
                Number r = (Number) right;
                Number l = (Number) left;

                boolean integerDivision = !(l instanceof Double || l instanceof Float ||
                        r instanceof Double || r instanceof Float);

                if (integerDivision) {
                    if (r.longValue() == 0) {
                        throw new RuntimeError("Division by zero.");
                    }
                    yield l.longValue() / r.longValue();
                }

                yield l.doubleValue() / r.doubleValue();
            }
            case PERCENT -> {
                checkNumberOperands(expr.op(), left, right);
                Number l = (Number) left;
                Number r = (Number) right;

                // reject mod by 0
                if (r.doubleValue() == 0.0) {
                    throw new RuntimeError("Modulo by zero.");
                }

                boolean isIntegerMod = !(l instanceof Double || l instanceof Float ||
                        r instanceof Double || r instanceof Float);

                if (isIntegerMod) {
                    yield l.longValue() % r.longValue();
                }

                yield l.doubleValue() % r.doubleValue();
            }

            // Comparison operators return Booleans!
            case LANGLE -> {
                checkNumberOperands(expr.op(), left, right);
                Number l = (Number) left;
                Number r = (Number) right;

                if (isIntegerOperation(l, r)) {
                    yield l.longValue() < r.longValue();
                }
                yield l.doubleValue() < r.doubleValue();
            }
            case RANGLE -> {
                checkNumberOperands(expr.op(), left, right);
                Number l = (Number) left;
                Number r = (Number) right;

                if (isIntegerOperation(l, r)) {
                    yield l.longValue() > r.longValue();
                }
                yield l.doubleValue() > r.doubleValue();
            }
            case LESS_EQUALS -> {
                checkNumberOperands(expr.op(), left, right);
                Number l = (Number) left;
                Number r = (Number) right;

                if (isIntegerOperation(l, r)) {
                    yield l.longValue() <= r.longValue();
                }
                yield l.doubleValue() <= r.doubleValue();
            }
            case GREATER_EQUALS -> {
                checkNumberOperands(expr.op(), left, right);
                Number l = (Number) left;
                Number r = (Number) right;

                if (isIntegerOperation(l, r)) {
                    yield l.longValue() >= r.longValue();
                }
                yield l.doubleValue() >= r.doubleValue();
            }

            case NOT_EQUALS -> !isEqual(left, right);
            case DOUBLE_EQUALS -> isEqual(left, right);

            default -> null;
        };
    }

    @Override
    public Object visitTernary(Expr.Ternary expr) {
        return null;
    }

    @Override
    public Object visitAssign(Expr.Assign expr) {
        Object newValue = evaluate(expr.value());
        Integer hops = locals.get(expr);

        Token varToken = expr.var();
        String name = (String) varToken.value();

        if (hops != null) {
            environment.assign(name, newValue, hops);
        } else {
            environment.assignGlobal(name, newValue);
        }
        return null;
    }

    @Override
    public Object visitLogical(Expr.Logical expr) {
        Token opTok = expr.op();
        return switch ((Operator) opTok.token()) {
            case AND -> {
                Object left = evaluate(expr.left());
                if (!(left instanceof Boolean lb)) {
                    throw new RuntimeError("Non boolean value " + left + " cannot be used in logical and ('&&').");
                }

                Object right = evaluate(expr.right());
                if (!(right instanceof Boolean rb)) {
                    throw new RuntimeError("Non boolean value " + right + " cannot be used in logical and ('&&').");
                }

                yield lb && rb;
            }
            case OR -> {
                Object left = evaluate(expr.left());
                if (!(left instanceof Boolean lb)) {
                    throw new RuntimeError("Non boolean value " + left + " cannot be used in logical or ('||').");
                }

                Object right = evaluate(expr.right());
                if (!(right instanceof Boolean rb)) {
                    throw new RuntimeError("Non boolean value " + right + " cannot be used in logical or ('||').");
                }

                yield lb || rb;
            }
            default -> throw new RuntimeError("Illegal logical operator.");
        };
    }

    @Override
    public Object visitGrouping(Expr.Grouping expr) {
        return null;
    }

    @Override
    public Object visitLiteral(Expr.Literal expr) {
        Object value = expr.value();
        if (value instanceof Token token) {

            Enum<?> tokenType = token.token();
            if (tokenType == TokenType.CHAR_LITERAL ||
                    tokenType == TokenType.NUMBER_LITERAL ||
                    tokenType == TokenType.STRING_LITERAL) {
                return token.value();
            }

            // handle keyword literals
            if (tokenType == Keyword.FALSE) return false;
            if (tokenType == Keyword.TRUE)  return true;
            if (tokenType == Keyword.NULL)  return null;

        }

        if (value instanceof String) {
            return value;
        }

        throw new RuntimeError("Unsupported literal object type: " + (value != null ? value.getClass().getName() : "null"));
    }

    @Override
    public Object visitIdentifier(Expr.Identifier expr) {
        String name = (String) expr.name().value();
        return lookUpVariable(name, expr);
    }

    @Override
    public Object visitStringConcat(Expr.StringConcat stringConcat) {
        List<Expr> concatenatedExpr = stringConcat.strings();
        StringBuilder sb = new StringBuilder();
        for (Expr expr : concatenatedExpr) {
            Object value = evaluate(expr);
            if (value == null) {
                throw new RuntimeError("Cannot concatenate null value.");
            }

            sb.append(value);
        }

        return sb.toString();
    }

    @Override
    public Object visitCall(Expr.Call expr) {
        return null;
    }

    @Override
    public Object visitArrayIndex(Expr.ArrayIndex expr) {
        return null;
    }

    @Override
    public Void visitEcho(Stmt.Echo stmt) {
        Object content = evaluate(stmt.expr());
        System.out.print(content);
        return null;
    }

    @Override
    public Object visitInput(Expr.Input stmt) {
        return sc.nextLine();
    }

    @Override
    public Object visitTypeCast(Expr.TypeCast expr) {
        Object value = evaluate(expr.expr());

        Token typeTok = expr.type();
        final String errorType = value == null ? "null" : value.getClass().getSimpleName();
        return switch ((Keyword) typeTok.token()) {
            case VOID -> null;
            case BOOLEAN -> isTruthy(value);
            case INTEGER -> {
                if (value instanceof String s) {
                    try {
                        yield Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        throw new RuntimeError("Cannot cast invalid string '" + s + "' to int.");
                    }
                }

                if (value instanceof Boolean b) {
                    yield b ? 1 : 0;
                }
                if (value instanceof Number n) {
                    yield n.intValue();
                }
                throw new RuntimeError("Invalid cast to integer from type: " + errorType);
            }
            case SHORT -> {
                if (value instanceof String s) {
                    try {
                        yield Short.parseShort(s);
                    } catch (NumberFormatException e) {
                        throw new RuntimeError("Cannot cast invalid string '" + s + "' to short.");
                    }
                }
                if (value instanceof Boolean b) {
                    yield b ? 1 : 0;
                }
                if (value instanceof Number n) {
                    yield n.shortValue();
                }
                throw new RuntimeError("Invalid cast to short from type: " + errorType);
            }
            case LONG -> {
                if (value instanceof String s) {
                    try {
                        yield Long.parseLong(s);
                    } catch (NumberFormatException e) {
                        throw new RuntimeError("Cannot cast invalid string '" + s + "' to long.");
                    }
                }

                if (value instanceof Boolean b) {
                    yield b ? 1 : 0;
                }
                if (value instanceof Number n) {
                    yield n.longValue();
                }
                throw new RuntimeError("Invalid cast to long from type: " + errorType);
            }
            case FLOAT -> {
                if (value instanceof String s) {
                    try {
                        yield Float.parseFloat(s);
                    } catch (NumberFormatException e) {
                        throw new RuntimeError("Cannot cast invalid string '" + s + "' to float.");
                    }
                }

                if (value instanceof Boolean b) {
                    yield b ? 1 : 0;
                }
                if (value instanceof Number n) {
                    yield n.floatValue();
                }
                throw new RuntimeError("Invalid cast to float from type: " + errorType);
            }
            case DOUBLE -> {
                if (value instanceof String s) {
                    try {
                        yield Double.parseDouble(s);
                    } catch (NumberFormatException e) {
                        throw new RuntimeError("Cannot cast invalid string '" + s + "' to double.");
                    }
                }

                if (value instanceof Boolean b) {
                    yield b ? 1 : 0;
                }
                if (value instanceof Number n) {
                    yield n.doubleValue();
                }
                throw new RuntimeError("Invalid cast to double from type: " + errorType);
            }
            case STR -> {
                if (value == null) {
                    yield "null";
                }

                yield String.valueOf(value);
            }
            case CHAR -> {
                if (value instanceof Character c) {
                    yield c;
                }
                if (value instanceof Number n) {
                    int code = n.intValue();

                    if (code < Character.MIN_VALUE || code > Character.MAX_VALUE) {
                        throw new RuntimeError("Numeric value " + code + " is out of bounds for a character.");
                    }

                    yield (char) code;
                }
                if (value instanceof String s) {
                    throw new RuntimeError("Invalid conversion to char from str " + s + ".");
                }
                throw new RuntimeError("Invalid cast to char from type: " + errorType);
            }
            default -> throw new RuntimeError("Unexpected type: " + typeTok.token());
        };
    }

    @Override
    public Void visitError(Stmt.Error stmt) {
        return null;
    }

    @Override
    public Void visitVarDecl(Stmt.VarDecl stmt) {
        String name = (String) stmt.name().value();

        Object value = null;
        Enum<?> type = stmt.type().token();
        if (!(type instanceof Keyword k)) {
            throw new RuntimeError("Invalid type " + type);
        }

        if (!Keyword.TYPES.contains(k)) {
            throw new RuntimeError("Invalid type " + k);
        }

        if (k == Keyword.VOID) {
            throw new RuntimeError("Cannot declare variable of type void.");
        }

        if (stmt.init() != null) {
            value = evaluate(stmt.init());
            switch (k) {
                case Keyword.BOOLEAN -> {
                    if (!(value instanceof Boolean)) {
                        throw new RuntimeError("Cannot assign non boolean value " + value + " to boolean variable.");
                    }
                }
                case Keyword.CHAR -> {
                    if (!(value instanceof Character)) {
                        throw new RuntimeError("Cannot assign non char value " + value + " to char variable.");
                    }
                }
                case Keyword.SHORT -> {
                    if (value instanceof Number n) {
                        if (n.doubleValue() % 1 != 0) {
                            throw new RuntimeError("Cannot assign decimal " + value + " to short variable.");
                        }
                        value = n.shortValue();
                    } else {
                        throw new RuntimeError("Cannot assign non short value " + value + " to short variable.");
                    }
                }
                case Keyword.INTEGER -> {
                    if (value instanceof Number n) {
                        if (n.doubleValue() % 1 != 0) {
                            throw new RuntimeError("Cannot assign decimal " + value + " to int variable.");
                        }
                        value = n.intValue();
                    } else {
                        throw new RuntimeError("Cannot assign non int value " + value + " to int variable.");
                    }
                }
                case Keyword.LONG -> {
                    if (value instanceof Number n) {
                        if (n.doubleValue() % 1 != 0) {
                            throw new RuntimeError("Cannot assign decimal " + value + " to long variable.");
                        }
                        value = n.longValue();
                    } else {
                        throw new RuntimeError("Cannot assign non long value " + value + " to long variable.");
                    }
                }
                case Keyword.FLOAT -> {
                    if (value instanceof Number n) {
                        value = n.floatValue();
                    } else {
                        throw new RuntimeError("Cannot assign non float value " + value + " to float variable.");
                    }
                }
                case Keyword.DOUBLE -> {
                    if (value instanceof Number n) {
                        value = n.doubleValue();
                    } else {
                        throw new RuntimeError("Cannot assign non double value " + value + " to double variable.");
                    }
                }
                case Keyword.STR -> {
                    if (!(value instanceof String)) {
                        throw new RuntimeError("Cannot assign non string value " + value + " to string variable.");
                    }
                }
            }
        }

        environment.define(name, value);
        return null;
    }

    @Override
    public Void visitExprStmt(Stmt.ExprStmt stmt) {
        evaluate(stmt.expr());
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.IfStmt stmt) {
        Expr condStmt = stmt.condition();
        Object condVal = evaluate(condStmt);
        boolean cond = isTruthy(condVal);

        if (cond) {
            execute(stmt.thenBranch());
        } else if (stmt.elseBranch() != null) {
            execute(stmt.elseBranch());
        }
        return null;
    }

    @Override
    public Void visitSwitchStmt(Stmt.SwitchStmt stmt) {
        return null;
    }

    @Override
    public Void visitCaseBlock(Stmt.CaseBlock stmt) {
        return null;
    }

    @Override
    public Void visitDefaultBlock(Stmt.DefaultBlock stmt) {
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.WhileStmt stmt) {
        while (isTruthy(evaluate(stmt.condition()))) {
            execute(stmt.content());

            if (controlSign == 1) {
                controlSign = 0;
                break;
            }

            if (controlSign == 2) {
                controlSign = 0;
            }
        }
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.ForStmt stmt) {
        Environment previous = this.environment;
        this.environment = new Environment(this.environment);

        try {

            if (stmt.init() != null) interpret(stmt.init());

            while (stmt.condition() == null || isTruthy(evaluate(stmt.condition()))) {
                execute(stmt.content());
                // break
                if (controlSign == 1) {
                    controlSign = 0;
                    break;
                }

                if (stmt.incrementer() != null) {
                    for (Expr incrementer : stmt.incrementer()) {
                        evaluate(incrementer);
                    }
                }
            }
        } finally {
            this.environment = previous;
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.ReturnStmt stmt) {
        return null;
    }

    @Override
    public Void visitBlock(Stmt.Block stmt) {
        Environment previous = this.environment;
        List<Stmt> stmts = stmt.statements();
        try {
            // define a new scope
            this.environment = new Environment(this.environment);

            for (int i = 0; i < stmts.size(); i++) {
                Stmt content = stmts.get(i);
                execute(content);

                if (controlSign != 0) {
                    break;
                }
            }
        } finally {
            this.environment = previous;
        }
        return null;
    }

    @Override
    public Void visitFunction(Stmt.Function stmt) {
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.BreakStmt stmt) {
        controlSign = 1;
        return null;
    }

    @Override
    public Void visitContinueStmt(Stmt.ContinueStmt stmt) {
        controlSign = 2;
        return null;
    }
}