package com.craftinginterpreters.lox;


/**
 * @author Javen-Liu
 * @version 1.0
 * @date 2020/6/17 16:57
 * @github https://github.com/Javen-Liu
 * 打印抽象树的具体表达式
 */
public class AstPrinter implements Expr.Visitor<String>{
    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return null;
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return null;
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        return null;
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return expr.value == null ? "nil" : expr.value.toString();
    }

    @Override
    public String visitLogicExpr(Expr.Logic expr) {
        return null;
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        return null;
    }

    @Override
    public String visitSuperExpr(Expr.Super expr) {
        return null;
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return null;
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return null;
    }

    @Override
    public String visitKeywordExpr(Expr.Keyword expr) {
        return null;
    }

    String print(Expr expr) {
        return expr.accept(this);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ").append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    /**
     * This main method is a test, that transforms an syntax abstract tree
     * into a normal string which we usually usr                                                            xx
     * example in this main method : (* (- 123) (group 45.67))
     */
    public static void main(String[] args) {
        Expr expr = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal("123")
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal("45.67")
                )
        );
        System.out.println(new AstPrinter().print(expr));
    }
}
