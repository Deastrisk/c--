import errors.ParseError;
import kotlin.random.AbstractPlatformRandom;
import rules.Expr;
import rules.Stmt;
import token.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    List<Token> tokens;
    private int cursor;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isEOF()) {
            Stmt declaration = declaration();
//            System.out.println(declaration);
            statements.add(declaration);
        }
        return statements;
    }

    private Stmt declaration() {
        if (check(
                Keyword.VOID,
                Keyword.BOOLEAN,
                Keyword.INTEGER,
                Keyword.SHORT,
                Keyword.LONG,
                Keyword.FLOAT,
                Keyword.DOUBLE,
                Keyword.CHAR,
                Keyword.STR)) {
            return varDeclaration();
        }
        return statement();
    }

    private Stmt varDeclaration() {
        Token type = advance();
        consume(Punctuation.COLON, "Expects ':' after type declaration.");
        Token name = consume(TokenType.IDENTIFIER, "Expects variable name.");

        // default value
        Expr init = null;
        if (match(Operator.EQUALS)) {
            init = expression();
        }
        consume(Punctuation.SEMICOLON, "Expects ';' after var declaration.");
        return new Stmt.VarDecl(type, name, init, null);
    }

    private Stmt statement() {
        if (match(Keyword.IF))           { return ifStmt(); }
        if (match(Punctuation.LBRACE))   { return blockStmt(); }
        if (match(Keyword.RETURN))       { return returnStmt(); }
        if (match(Keyword.WHILE))        { return whileStmt(); }
        if (match(Keyword.FOR))          { return forStmt(); }
        if (match(Keyword.SWITCH))       { return switchStmt(); }
        if (match(Keyword.ECHO))         { return echoStmt(); }
        if (match(Keyword.FN))           { return functionStmt(); }
//        if (check(TokenType.IDENTIFIER)) { return identifierStmt(); }
        if (match(Keyword.CONTINUE))     { return continueStmt(); }
        if (match(Keyword.BREAK))        { return breakStmt(); }
        return exprStatement();
    }

    private Stmt exprStatement() {
        Expr expr = expression();
        consume(Punctuation.SEMICOLON, "Expects ';' after expression.");
        return new Stmt.ExprStmt(expr);
    }

    private Stmt continueStmt() {
        consume(Punctuation.SEMICOLON, "Expects ';' after continue statement.");
        return new Stmt.ContinueStmt();
    }

    private Stmt breakStmt() {
        consume(Punctuation.SEMICOLON, "Expects ';' after break expression.");
        return new Stmt.BreakStmt();
    }

    private Stmt identifierStmt() {
        return exprStatement();
    }

    private Stmt functionStmt() {
        consume(Punctuation.COLON, "Expects ':' after function declaration.");
        Token identifier = consume(TokenType.IDENTIFIER, "Expects function identifier after declaration.");
        consume(Punctuation.LPAREN, "Expects '(' to start function parameters.");
        List<Stmt.Parameter> params = fnParamsStmt();
        consume(Punctuation.RPAREN, "Expects ')' to end function parameters.");
        consume(Punctuation.ARROW, "Expects '->' after function parameters.");
        // Assuming your types are keywords like Keyword.INT, Keyword.STR, etc.
        Token returnType = advance(); // Or use a dedicated parseType() method if complex

        // Parse the body: { ... }
        // Since your statement() method already matches LBRACE to blockStmt(),
        // we can reuse that logic here.
        consume(Punctuation.LBRACE, "Expects '{' before function body.");
        Stmt body = blockStmt();

        return new Stmt.Function(returnType, identifier, params, body);
    }

    private List<Stmt.Parameter> fnParamsStmt() {
        List<Stmt.Parameter> params = new ArrayList<>();

        boolean lookingAtDefaultParams = false;
        if (!check(Punctuation.RPAREN)) {
            do {
                if (params.size() >= 255) {
                    throw new ParseError(peek(), "Can't have more than 255 parameters.");
                }

                Token type = advance();
                consume(Punctuation.COLON, "Expects ':' after type declaration.");
                Token name = consume(TokenType.IDENTIFIER, "Expects parameter name.");

                Expr dVal = null;
                if (match(Operator.EQUALS)) {
                    dVal = expression();
                    lookingAtDefaultParams = true;
                } else if (lookingAtDefaultParams) {
                    throw new ParseError(peek(), "Required parameter cannot follow a parameter with default values.");
                }

                params.add(new Stmt.Parameter(type, name, dVal));
            } while (match(Punctuation.COMMA));
        }

        return params;
    }

    private Stmt switchStmt() {
        consume(Punctuation.LPAREN, "Expects '(' after switch.");
        Expr condition = expression();
        consume(Punctuation.RPAREN, "Expects ')' after switch.");
        consume(Punctuation.LBRACE, "Expects '{' in switch statement.");
        List<Stmt> caseBlock = caseBlockStmt();
        return new Stmt.SwitchStmt(condition, caseBlock);
    }

    private List<Stmt> caseBlockStmt() {
        List<Stmt> caseBlock = new ArrayList<>();
        while (!isEOF() && !check(Punctuation.RBRACE) && !check(Keyword.DEFAULT)) {
            consume(Keyword.CASE, "Expects \"case\" keyword after switch expression");
            caseBlock.add(caseStmt());
        }

        if (match(Keyword.DEFAULT)) {
            caseBlock.add(defaultStmt());
        }
        consume(Punctuation.RBRACE, "Expects '}' after case block.");
        return caseBlock;
    }

    private Stmt caseStmt() {
        Expr expr = expression();
        consume(Punctuation.ARROW, "Expects \"->\" after case expression.");
        Stmt body = statement();
        return new Stmt.CaseBlock(expr, body);
    }

    private Stmt defaultStmt() {
        consume(Punctuation.ARROW, "Expects \"->\" after default expression.");
        Stmt body = statement();
        return new Stmt.DefaultBlock(body);
    }

    private Stmt returnStmt() {
        Expr expr = expression();
        consume(Punctuation.SEMICOLON, "Expects ';' after expression");
        return new Stmt.ReturnStmt(expr);
    }

    private Stmt blockStmt() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(Punctuation.RBRACE) && !isEOF()) {
            Stmt statement = declaration();
//            System.out.println(statement);
            statements.add(statement);
        }

        consume(Punctuation.RBRACE, "Expects '}' to close statement.");
        return new Stmt.Block(statements);
    }

    private Stmt echoStmt() {
        consume(Punctuation.LPAREN, "Expects '(' to start echo.");
        Expr content = expression();
        consume(Punctuation.RPAREN, "Expects ')' to close echo.");
        consume(Punctuation.SEMICOLON, "Expects ';' after echo statement.");
        return new Stmt.Echo(content);
    }

    private Stmt forStmt() {
        consume(Punctuation.LPAREN, "Expects '(' to start for condition.");

        if (check(Keyword.INTEGER, Keyword.BOOLEAN, Keyword.SHORT, Keyword.LONG, Keyword.FLOAT, Keyword.DOUBLE, Keyword.STR)) {
            return defaultForStmt();
        }

//        if (check(TokenType.IDENTIFIER)) {
//            return forEachStmt();
//        }

        throw new ParseError(previous(), "Expects for, for each, or in-range loop.");
    }

//    private Stmt forEachStmt() {
//
//    }

    private Stmt defaultForStmt() {
        List<Stmt> init = forInitStmt();
        consume(Punctuation.SEMICOLON, "Expects ';' after for initializers.");

        Expr condition = null;
        if (!check(Punctuation.SEMICOLON)) {
            condition = expression();
        }
        consume(Punctuation.SEMICOLON, "Expects ';' after for condition.");
        List<Expr> incrementers = forIncrementersExpr();
        consume(Punctuation.RPAREN, "Expects ')' after for incrementer.");
        Stmt content = statement();
        return new Stmt.ForStmt(init, condition, incrementers, content);
    }

    private List<Expr> forIncrementersExpr() {
        List<Expr> incrementers = new ArrayList<>();
        while (!isEOF() && !check(Punctuation.RPAREN)) {
            incrementers.add(expression());
            if (!match(Punctuation.COMMA)) {
                break;
            }
        }
        return incrementers;
    }

    private List<Stmt> forInitStmt() {
        List<Stmt> inits = new ArrayList<>();
        while (!isEOF() && !check(Punctuation.SEMICOLON)) {
            if (check(Keyword.INTEGER, Keyword.BOOLEAN, Keyword.SHORT, Keyword.LONG, Keyword.FLOAT, Keyword.DOUBLE, Keyword.STR)) {
                inits.add(forDeclarationStmt());
            } else {
                throw new ParseError(peek(), "Invalid type.");
            }
            if (!match(Punctuation.COMMA)) {
                break;
            }
        }
        return inits;
    }

    private Stmt forDeclarationStmt() {
        return forVarDeclaration();
    }

    private Stmt forVarDeclaration() {
        Token type = advance();
        consume(Punctuation.COLON, "Expects ':' after type");
        Token var = consume(TokenType.IDENTIFIER, "Expects identifier in declaration.");
        consume(Operator.EQUALS, "Expects '=' after variable identifier.");
        Expr init = expression();
        return new Stmt.VarDecl(type, var, init, null);
    }

    private Stmt whileStmt() {
        consume(Punctuation.LPAREN, "Expects '(' after while keyword");
        Expr condition = expression();
        consume(Punctuation.RPAREN, "Expects ')' after while condition");
        Stmt content = statement();
        return new Stmt.WhileStmt(condition, content);
    }

    private Stmt ifStmt() {
        consume(Punctuation.LPAREN, "Expects '(' after if keyword.");
        Expr condition = expression();
        consume(Punctuation.RPAREN, "Expects ')' after if condition.");
        Stmt thenBranch = statement();

        Stmt elseBranch = null;
        if (match(Keyword.ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.IfStmt(condition, thenBranch, elseBranch);
    }

    private Expr inputExpr() {
        consume(Punctuation.LPAREN, "Expects '(' to start input.");
        consume(Punctuation.RPAREN, "Expects ')' to close input.");
        return new Expr.Input();
    }

    private Expr expression() {
        return assignment();
    }

    private Operator convertToBinaryOp(Operator op, Token opTok) {
        return switch (op) {
            case Operator.MINUS_EQUALS -> Operator.MINUS;
            case Operator.PLUS_EQUALS -> Operator.PLUS;
            case Operator.STAR_EQUALS -> Operator.STAR;
            case Operator.SLASH_EQUALS -> Operator.SLASH;
            default -> throw new ParseError(opTok, "Unknown assignment operator " + op + ".");
        };
    }

    private Expr assignment() {
        Token identifierToken = peek();
        Expr expr = ternary();

        if (match(Operator.EQUALS, Operator.PLUS_EQUALS, Operator.SLASH_EQUALS, Operator.STAR_EQUALS, Operator.MINUS_EQUALS)) {
            Token opTok = previous();
            Operator op = (Operator) opTok.token();
            Expr right = assignment();

            if (expr instanceof Expr.Identifier varExpr) {
                Token name = varExpr.name();

                if (op != Operator.EQUALS) {
                    Operator binaryOp = convertToBinaryOp(op, opTok);
                    right = new Expr.Binary(expr, binaryOp, right);
                }

                return new Expr.Assign(name, right);
            }

            throw new ParseError(identifierToken, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr ternary() {
        Expr expr = logicalOr();

        if (match(Operator.QUESTION)) {
            Expr thenBranch = expression();
            consume(Punctuation.COLON, "Expects colon separating ternary operator.");
            Expr elseBranch = assignment();

            return new Expr.Ternary(expr, thenBranch, elseBranch);
        }

        return expr;
    }

    private Expr logicalOr() {
        Expr expr = logicalAnd();

        if (match(Operator.OR)) {
            Token op = previous();
            Expr right = logicalOr();
            expr = new Expr.Logical(expr, op, right);
        }

        return expr;
    }

    private Expr logicalAnd() {
        Expr expr = equality();

        if (match(Operator.AND)) {
            Token op = previous();
            Expr right = logicalAnd();
            expr = new Expr.Logical(expr, op, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        if (match(Operator.DOUBLE_EQUALS, Operator.NOT_EQUALS)) {
            Operator op = (Operator) previous().token();
            Expr right = equality();
            expr = new Expr.Binary(expr, op, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        if (match(Operator.LESS_EQUALS, Operator.GREATER_EQUALS, Operator.LANGLE, Operator.RANGLE)) {
            Operator op = (Operator) previous().token();
            Expr right = comparison();
            expr = new Expr.Binary(expr, op, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(Operator.PLUS, Operator.MINUS)) {
            Operator op = (Operator) previous().token();
            Expr right = term();

            // string concatenation
            if (op == Operator.PLUS && (Expr.isStringExpr(expr) || Expr.isStringExpr(right))) {
                expr = flattenStringConcat(expr, right);
            } else {
                expr = new Expr.Binary(expr, op, right);
            }
        }

        return expr;
    }

    private Expr flattenStringConcat(Expr left, Expr right) {
        List<Expr> values = new ArrayList<>();

        if (left instanceof Expr.StringConcat concat) {
            values.addAll(concat.strings());
        } else {
            values.add(left);
        }

        if (right instanceof Expr.StringConcat concat) {
            values.addAll(concat.strings());
        } else {
            values.add(right);
        }

        return new Expr.StringConcat(values);
    }

    private Expr factor() {
        Expr expr = prefix();

        while (match(Operator.STAR, Operator.SLASH, Operator.PERCENT)) {
            Operator op = (Operator) previous().token();
            Expr right = factor();
            expr = new Expr.Binary(expr, op, right);
        }

        return expr;
    }

    private Expr typeCast() {
        Token type = advance();
        consume(Punctuation.RPAREN, "Expects parentheses after typecast operator.");

        Expr value = expression();

        return new Expr.TypeCast(type, value);
    }

    private Expr prefix() {
        if (match(Punctuation.LPAREN)) {
            if (check(Keyword.VOID, Keyword.BOOLEAN, Keyword.SHORT, Keyword.CHAR, Keyword.INTEGER, Keyword.LONG, Keyword.FLOAT, Keyword.DOUBLE, Keyword.STR)) {
                return typeCast();
            }
        }

        if (match(Operator.MINUS, Operator.EXCLAMATION, Operator.DOUBLE_PLUS, Operator.DOUBLE_MINUS)) {
            Operator op = (Operator) previous().token();
            Expr expr = prefix();
            return new Expr.Prefix(op, expr);
        }

        return postfix();
    }

    private Expr postfix() {
        Expr expr = primary();

        while (true) {
            if (match(Punctuation.LPAREN)) {
                expr = finishCallExpr(expr);
            }
            else if (match(Punctuation.LBRACKET)) {
                Expr index = expression();
                consume(Punctuation.RBRACKET, "Expects ']' after array index.");

                expr = new Expr.ArrayIndex(expr, index);
            }
            else {
                break;
            }
        }

        if (match(Operator.DOUBLE_MINUS, Operator.DOUBLE_PLUS)) {
            Operator op = (Operator) previous().token();
            expr = new Expr.Postfix(expr, op);
        }

        return expr;
    }

    private Expr finishCallExpr(Expr callee) {
        List<Expr> arguments = parseArguments();
        Token paren = consume(Punctuation.RPAREN, "Expects ')' after arguments.");
        return new Expr.Call(callee, arguments);
    }

    private List<Expr> parseArguments() {
        List<Expr> arguments = new ArrayList<>();
        if (!check(Punctuation.RPAREN)) {
            do {
                if (arguments.size() >= 255) {
                    throw new ParseError(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(Punctuation.COMMA));
        }
        return arguments;
    }

    private Expr primary() {
        // handle literals: values of data types
        if (match(
                TokenType.NUMBER_LITERAL,
                TokenType.CHAR_LITERAL,
                Keyword.FALSE,
                Keyword.TRUE,
                Keyword.NULL)) {
            return new Expr.Literal(previous());
        }

        if (match(Keyword.INPUT)) {
            return inputExpr();
        }

        if (match(TokenType.STRING_LITERAL)) {
            return stringExpr();
        }

        // handle identifiers: variable names
        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Identifier(previous());
        }

        // handle grouping: ( expr )
        if (match(Punctuation.LPAREN)) {
            Expr expr = expression();
            consume(Punctuation.RPAREN, "Expects ')' after expression");
            return new Expr.Grouping(expr);
        }

//        System.out.println(peek());
        throw new ParseError(peek(), "Expected expression (literal, identifier, or group) at token: " + peek());
    }

    private Expr stringExpr() {
        List<Expr> str = new ArrayList<>();
        str.add(new Expr.Literal(previous()));

        while (!isEOF() && match(TokenType.STR_FIELD_START)) {
            Operator op = Operator.PLUS;
            Expr field = expression();

            consume(TokenType.STR_FIELD_END, "Expects '}' to close string interpolation field.");

            str.add(field);

            if (match(TokenType.STRING_LITERAL)) {
                Expr right = new Expr.Literal(previous());
                str.add(right);
            }
        }

        return new Expr.StringConcat(str);
    }

    private Token peek() {
        return tokens.get(cursor);
    }

    private boolean isEOF() {
        return peek().token() == TokenType.EOF;
    }

    private boolean check(Object... types) {
        for (Object type : types) {
            if (check(type)) {
                return true;
            }
        }

        return false;
    }

    private boolean check(Object type) {
        if (isEOF()) return false;
        return peek().token() == type;
    }

    private Token advance() {
        if (!isEOF()) cursor++;
        return previous();
    }

    private boolean match(Object... types) {
        for (Object type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(Object type, String message) {
        if (check(type)) return advance();
        throw new ParseError(previous(), message);
    }

    private Token previous() {
        return tokens.get(cursor - 1);
    }
}
