package rules;

import token.Keyword;
import token.Token;

import javax.xml.stream.FactoryConfigurationError;
import java.util.List;
import java.util.Set;

public sealed interface Stmt extends ASTNode
        permits Stmt.Echo, Stmt.VarDecl, Stmt.ExprStmt, Stmt.IfStmt,
        Stmt.Block, Stmt.ReturnStmt, Stmt.WhileStmt, Stmt.ForStmt,
        Stmt.SwitchStmt, Stmt.CaseBlock, Stmt.DefaultBlock, Stmt.Error,
        Stmt.Function, Stmt.Parameter, Stmt.BreakStmt, Stmt.ContinueStmt {

    <R> R accept(Visitor<R> visitor);

    record Echo(Expr expr) implements Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitEcho(this);
        }
    }

    record Error(Expr expr) implements Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitError(this);
        }
    }

    record VarDecl(Token type, Token name, Expr init, Set<Keyword> modifiers) implements Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarDecl(this);
        }
    }

    record ExprStmt(Expr expr) implements Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExprStmt(this);
        }
    }

    record IfStmt(Expr condition, Stmt thenBranch, Stmt elseBranch) implements Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    record SwitchStmt(Expr condition, List<Stmt> cases) implements Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSwitchStmt(this);
        }
    }

    record CaseBlock(Expr caseExpr, Stmt body) implements Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCaseBlock(this);
        }
    }

    record DefaultBlock(Stmt body) implements Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitDefaultBlock(this);
        }
    }

    record WhileStmt(Expr condition, Stmt content) implements Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    record ForStmt(List<Stmt> init, Expr condition, List<Expr> incrementer, Stmt content) implements Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitForStmt(this);
        }
    }

    record ReturnStmt(Expr value) implements Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    record Block(List<Stmt> statements) implements Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlock(this);
        }
    }

    record Function(Token type, Token name, List<Stmt.Parameter> params, Stmt body) implements Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunction(this);
        }
    }

    record Parameter(Token type, Token name, Expr defaultValue) implements Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return null;
        }
    }

    record BreakStmt() implements Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBreakStmt(this);
        }
    }

    record ContinueStmt() implements Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitContinueStmt(this);
        }
    }

    // for each is yet to be added
//    record ForEachStmt(String iterator, Expr collection, Stmt body) implements Stmt {
//        @Override
//        public <R> R accept(Visitor<R> visitor) {
//            return visitor.visitForEachStmt(this);
//        }
//    }

    interface Visitor<R> {
        R visitEcho(Echo stmt);
        R visitError(Error stmt);
        R visitVarDecl(VarDecl stmt);
        R visitExprStmt(ExprStmt stmt);
        R visitIfStmt(IfStmt stmt);
        R visitSwitchStmt(SwitchStmt stmt);
        R visitCaseBlock(CaseBlock stmt);
        R visitDefaultBlock(DefaultBlock stmt);
        R visitWhileStmt(WhileStmt stmt);
        R visitForStmt(ForStmt stmt);
        R visitReturnStmt(ReturnStmt stmt);
        R visitBlock(Block stmt);
        R visitFunction(Function stmt);
        R visitBreakStmt(BreakStmt stmt);
        R visitContinueStmt(ContinueStmt stmt);
//        R visit(Function stmt);
    }
}
