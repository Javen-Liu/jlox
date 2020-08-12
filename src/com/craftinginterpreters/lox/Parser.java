package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

/**
 * @author Javen-Liu
 * @version 1.0
 * @date 2020/6/18 15:40
 * @github https://github.com/Javen-Liu
 * 在Lox中，程序（Programing）是由多个declaration（声明）语句
 * 组成的，而声明语句是由 表达式 和 赋值声明语句 组成的。而表达式
 * 又分为了语句表达式（exprStmt）和输出表达式（printStmt）
 */
public class Parser {
    private static class ParseErrorException extends RuntimeException{

    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * 解析送入的Token列表，将其转换为语法树表达式
     * @return 语法树表达式的列表
     */
    List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    /**
     * declaration（声明）在概念上也属于statement，但是其与上面方法中的statement不同，属于平级关系
     * @return 如果该语句直接以 VAR 开头，则返回 VAR 声明表达式
     *         否则返回statement表达式
     */
    private Stmt declaration(){
        try {
            if (match(CLASS)) {
                return classDeclaration();
            }
            if (match(VAR)) {
                return varDeclaration();
            }

            if (match(FUN)) {
                return function("function");
            }

            return statement();
        } catch (ParseErrorException error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration(){
        Token name = consume(IDENTIFIER, "Expect class name.");
        consume(LEFT_BRACE, "Expect '{' before class body.");

        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"));
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.");

        return new Stmt.Class(name, methods);
    }

    private Stmt varDeclaration(){
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Except ';' after variable declaration");
        return new Stmt.Var(name, initializer);
    }

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");

        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");

        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Cannot have more than 255 parameters");
                }

                parameters.add(consume(IDENTIFIER, "Expect parameter name"));
            } while (match(COMMA));
        }

        consume(RIGHT_PAREN, "Expect ')' after parameters");

        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();

        return new Stmt.Function(name, parameters, body);
    }

    /**
     * 判断当前一句表达式是普通表达式,print表达式还是if表达式
     * @return 如果是if表达式，则返回控制流if语法树表达式
     *         如果是print表达式，则返回print表达式语法树表达式
     *         如果是大括号表达式，则返回block块语法树表达式
     *         如果是普通表达式，则返回普通语法树表达式
     */
    private Stmt statement() {
        if (match(FOR)) {
            return forStatement();
        }

        if (match(IF)) {
            return ifStatement();
        }

        if (match(PRINT)) {
            return printStatement();
        }

        if (match(RETURN)) {
            return returnStatement();
        }

        if (match(WHILE)) {
            return whileStatement();
        }

        if (match(LEFT_BRACE)) {
            return new Stmt.Block(block());
        }

        return expressionStatement();
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(SEMICOLON)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");

        Stmt body = statement();

        if (increment != null) {
            body = new Stmt.Block(
                    Arrays.asList(body, new Stmt.Expression(increment))
            );
        }

        if (condition == null) {
            condition = new Expr.Literal(true);
        }
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            body = new Stmt.Block(
                    Arrays.asList(initializer, body)
            );
        }

        return body;
    }

    private Stmt ifStatement(){
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;

        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }


    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Except '}' after block.");
        return statements;
    }

    private Stmt expressionStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Expression(value);
    }

    /**
     * 递归语法树，包括了
     * assignment     : 赋值表达式
     * or             : 或
     * and            : 与
     * equality       : 相等判断表达式
     * comparison     : 比较表达式
     * addition       : 加减表达式
     * multiplication : 乘除表达式
     * unary          : 二元表达式
     * call           : 方法调用表达式
     * primary        : 基本表达式
     */

    private Expr expression(){
        return assignment();
    }

    private Expr assignment(){
        Expr expr = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            }

            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr or(){
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logic(expr, operator, right);
        }

        return expr;
    }

    private Expr and(){
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logic(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = addition();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr addition() {
        Expr expr = multiplication();

        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr multiplication() {
        Expr expr = unary();

        while (match(STAR, SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS, MINUS_MINUS, PLUS_PLUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr call(){
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 256) {
                    error(peek(), "Cannot have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        switch (tokens.get(current++).type) {
            case FALSE:
                return new Expr.Literal(false);
            case TRUE:
                return new Expr.Literal(true);
            case NIL:
                return new Expr.Literal(null);
            case NUMBER:
            case STRING:
                return new Expr.Literal(previous().literal);
            case THIS:
                return new Expr.This(previous());
            case IDENTIFIER:
                return new Expr.Variable(previous());
            case LEFT_PAREN:
                Expr expr = expression();
                consume(RIGHT_PAREN, "Expect ')' after expression.");
                return new Expr.Grouping(expr);
            case BREAK:
            case CONTINUE:
                return new Expr.Keyword(tokens.get(current - 1));
            default:
                throw error(peek(), "Expect expression.");
        }
    }

    /**************************************************************************/

    /**
     * 以下是工具方法，包含了match，check，advance，isAtEnd，peek，previous和consume
     * match    : 判断当前current指针指向的Token是否满足传入的类型参数（可传入多个类型参数），如果符合则使指针向后移一位
     * check    : 与check类似，只不过match可以指定多个类型参数，而check则是具体进行判断的方法
     * advance  : 返回当前指针指向的Token对象，然后指针向后挪动一位
     * isAtEnd  : 判断当前所有的Token对象是否都已经被读取
     * peek     : 与match类似，但是不会使指针向后移一位
     * previous : 获取当前指针指向的前一个Token对象
     * consume  : 指针向后挪动一位，即弃置当前指向的Token对象
     */

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if(isAtEnd()){
            return false;
        }
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous(){
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message){
        if (check(type)) {
            return advance();
        }

        throw error(peek(), message);
    }

    /**************************************************************************/

    private ParseErrorException error(Token token, String message) {
        Lox.error(token, message);
        return new ParseErrorException();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) {
                return;
            }

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
                default:
            }

            advance();
        }
    }

}
