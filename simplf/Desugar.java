package simplf;

import java.util.ArrayList;
import java.util.List;
import simplf.Expr.Assign;
import simplf.Expr.Binary;
import simplf.Expr.Call;
import simplf.Expr.Conditional;
import simplf.Expr.Grouping;
import simplf.Expr.Literal;
import simplf.Expr.Logical;
import simplf.Expr.Unary;
import simplf.Expr.Variable;
import simplf.Stmt.Block;
import simplf.Stmt.Expression;
import simplf.Stmt.For;
import simplf.Stmt.Function;
import simplf.Stmt.If;
import simplf.Stmt.Print;
import simplf.Stmt.Var;
import simplf.Stmt.While;

public class Desugar implements Expr.Visitor<Expr>, Stmt.Visitor<Stmt> {

    public Desugar() {
    }

    public List<Stmt> desugar(List<Stmt> stmts) {
        ArrayList<Stmt> ret = new ArrayList<>();
        for (Stmt stmt : stmts) {
            ret.add(stmt.accept(this));
        }
        return ret;
    }

    @Override
    public Stmt visitPrintStmt(Print stmt) {
        return stmt;
    }

    @Override
    public Stmt visitExprStmt(Expression stmt) {
        return new Stmt.Expression(stmt.expr.accept(this));
    }

    @Override
    public Stmt visitVarStmt(Var stmt) {
        return new Var(stmt.name, stmt.initializer.accept(this));
    }

    @Override
    public Stmt visitBlockStmt(Block stmt) {
        ArrayList<Stmt> new_statements = new ArrayList<>();
        for (Stmt old_state : stmt.statements) {
            new_statements.add(old_state.accept(this));
        }
        return new Block(new_statements);
    }

    @Override
    public Stmt visitIfStmt(If stmt) {
        Stmt new_else;
        if (stmt.elseBranch == null) {
            new_else = null;
        } else {
            new_else = stmt.elseBranch.accept(this);
        }

        return new If(stmt.cond.accept(this),
                stmt.thenBranch.accept(this),
                new_else);
    }

    @Override
    public Stmt visitWhileStmt(While stmt) {
        return new While(stmt.cond.accept(this),
                stmt.body.accept(this));
    }

    @Override
    public Stmt visitForStmt(For stmt) {
        // Desugar for(init; cond; update) body
        // into:
        // {
        //   init;
        //   while (cond) {
        //     body;
        //     update;
        //   }
        // }

        // Desugar inner parts recursively
        Stmt initStmt = (stmt.init != null) ? new Stmt.Expression(stmt.init.accept(this)) : null;

    // Desugar condition (defaults to true)
    Expr cond = (stmt.cond != null) ? stmt.cond.accept(this) : new Expr.Literal(true);

    // Desugar increment (if any)
    Expr incr = (stmt.incr != null) ? stmt.incr.accept(this) : null;

    // Desugar body
    Stmt body = stmt.body.accept(this);

    // Append increment to body block
    if (incr != null) {
        List<Stmt> bodyPlusIncr = new ArrayList<>();
        bodyPlusIncr.add(body);
        bodyPlusIncr.add(new Stmt.Expression(incr));
        body = new Stmt.Block(bodyPlusIncr);
    }

    // Create while loop with condition and body
    Stmt whileLoop = new Stmt.While(cond, body);

    // Wrap with init statement if present
    if (initStmt != null) {
        List<Stmt> full = new ArrayList<>();
        full.add(initStmt);
        full.add(whileLoop);
        return new Stmt.Block(full);
    } else {
        return whileLoop;
    }
    }

    @Override
    public Stmt visitFunctionStmt(Function stmt) {
        ArrayList<Stmt> new_body = new ArrayList<>();
        for (Stmt old_statement : stmt.body) {
            new_body.add(old_statement.accept(this));
        }

        return new Function(stmt.name, stmt.params, new_body);
    }

    @Override
    public Expr visitBinary(Binary expr) {
        return new Binary(expr.left.accept(this), expr.op, expr.right.accept(this));
    }

    @Override
    public Expr visitUnary(Unary expr) {
        return new Unary(expr.op, expr.right.accept(this));
    }

    @Override
    public Expr visitLiteral(Literal expr) {
        return expr;
    }

    @Override
    public Expr visitGrouping(Grouping expr) {
        return new Grouping(expr.expression.accept(this));
    }

    @Override
    public Expr visitVarExpr(Variable expr) {
        return expr;
    }

    @Override
    public Expr visitAssignExpr(Assign expr) {
        return new Assign(expr.name, expr.value.accept(this));
    }

    @Override
    public Expr visitLogicalExpr(Logical expr) {
        return new Logical(expr.left.accept(this), expr.op, expr.right.accept(this));
    }

    @Override
    public Expr visitConditionalExpr(Conditional expr) {
        return new Conditional(expr.cond.accept(this),
                expr.thenBranch.accept(this),
                expr.elseBranch.accept(this));
    }

    @Override
    public Expr visitCallExpr(Call expr) {
        ArrayList<Expr> new_args = new ArrayList<>();
        for (Expr arg : expr.args) {
            new_args.add(arg.accept(this));
        }

        return new Call(expr.callee.accept(this), expr.paren, new_args);
    }
}
