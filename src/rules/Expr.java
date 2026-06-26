package rules;

import token.Operator;
import token.Token;

import java.util.List;

public sealed interface Expr extends ASTNode
        permits Expr.Literal, Expr.Grouping, Expr.Binary, Expr.Identifier,
        Expr.Prefix, Expr.Postfix, Expr.StringConcat, Expr.Ternary, Expr.Assign,
        Expr.Logical, Expr.Call, Expr.ArrayIndex, Expr.Input, Expr.TypeCast {

    <R> R accept(Visitor<R> visitor);

    record Prefix(Operator op, Expr right) implements Expr {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrefix(this);
        }
    }
    record Postfix(Expr left, Operator op) implements Expr {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPostfix(this);
        }
    }
    record Binary(Expr left, Operator op, Expr right) implements Expr {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinary(this);
        }
    }
    record Ternary(Expr condition, Expr thenBranch, Expr elseBranch) implements Expr {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTernary(this);
        }
    }
    record Assign(Token var, Expr value) implements Expr {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssign(this);
        }
    }
    record Logical(Expr left, Token     op, Expr right) implements Expr {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogical(this);
        }
    }
    record Grouping(Expr expression) implements Expr {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGrouping(this);
        }
    }
    record Literal(Object value) implements Expr {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteral(this);
        }
    }
    record Identifier(Token name) implements Expr {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIdentifier(this);
        }
    }
    record StringConcat(List<Expr> strings) implements Expr {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStringConcat(this);
        }
    }
    record Call(Expr callee, List<Expr> arguments) implements Expr {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCall(this);
        }
    }
    record ArrayIndex(Expr expr, Expr index) implements Expr {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayIndex(this);
        }
    }
    record Input() implements Expr {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitInput(this);
        }
    }
    record TypeCast(Token type, Expr expr) implements Expr {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTypeCast(this);
        }
    }

    static boolean isStringExpr(Expr expr) {
        return expr instanceof StringConcat;
    }

    interface Visitor<R> {
        R visitPrefix(Prefix expr);
        R visitPostfix(Postfix expr);
        R visitBinary(Binary expr);
        R visitTernary(Ternary expr);
        R visitAssign(Assign expr);
        R visitLogical(Logical expr);
        R visitGrouping(Grouping expr);
        R visitLiteral(Literal expr);
        R visitIdentifier(Identifier expr);
        R visitStringConcat(StringConcat expr);
        R visitCall(Call expr);
        R visitArrayIndex(ArrayIndex expr);
        R visitInput(Input expr);
        R visitTypeCast(TypeCast expr);
    }
}
