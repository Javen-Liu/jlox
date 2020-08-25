package com.craftinginterpreters.lox;

import jdk.nashorn.internal.parser.Lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Javen-Liu
 * @version 1.0
 * @date 2020/6/22 21:23
 * @github https://github.com/Javen-Liu
 */
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{
    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();
    private boolean isBreak = false;
    private boolean isContinue = false;

    Interpreter(){
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000d;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    /**
     * Expr的visit方法
     */

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }

        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperand(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperand(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return (double)left <= (double)right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case MINUS:
                checkNumberOperand(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (isDoubleNum(left, right)) {
                    return (double)left + (double)right;
                }
                // if (left instanceof String && right instanceof String) {
                //     return (String) left + (String) right;
                // }
                // throw new RuntimeError(expr.operator,
                //         "Operands must be two numbers or two strings.");
                return transformToString(left, right);
            case STAR:
                checkNumberOperand(expr.operator, left, right);
                return (double)left * (double)right;
            case SLASH:
                checkNumberOperand(expr.operator, left, right);
                if ((double) right == 0) {
                    throw new RuntimeError(expr.operator, "divisor should not be 0");
                }
                return (double)left / (double)right;
            default:
        }
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable)callee;

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expect" +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof LoxInstance) {
            return ((LoxInstance) object).get(expr.name);
        }

        if (object instanceof LoxClass) {
            return ((LoxClass) object).findStaticMethod(expr.name.lexeme);
        }

        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return expr.expression;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicExpr(Expr.Logic expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) {
                return left;
            }
        }else{
            if (!isTruthy(left)) {
                return left;
            }
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof LoxInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields");
        }

        Object value = evaluate(expr.value);
        ((LoxInstance) object).set(expr.name, value);
        return null;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);
        LoxClass superclass = (LoxClass)environment.getAt(distance, "super");

        LoxInstance object = (LoxInstance) environment.
                getAt(distance - 1, "this");

        LoxFunction method = superclass.findMethod(expr.method.lexeme);

        if (method == null) {
            throw new RuntimeError(expr.method,
                    "Undefined property '" + expr.method.lexeme + "'.");
        }

        return method.bind(object);
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        Expr.Variable identifier;

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            case MINUS_MINUS:
                checkNumberOperand(expr.operator, right);
                identifier = (Expr.Variable) expr.right;
                environment.assign(identifier.name, (double)environment.get(identifier.name) - 1);
                return (double)right - 1;
            case PLUS_PLUS:
                checkNumberOperand(expr.operator, right);
                identifier = (Expr.Variable) expr.right;
                environment.assign(identifier.name, (double)environment.get(identifier.name) + 1);
                return (double)right + 1;
            default:
        }

        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    @Override
    public Object visitKeywordExpr(Expr.Keyword expr) {
        if (TokenType.BREAK == expr.name.type) {
            isBreak = true;
        }

        if (TokenType.CONTINUE == expr.name.type) {
            isContinue = true;
        }

        return null;
    }

    /**************************************************************************/



    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (boolean) object;
        }
        return true;
    }

    private boolean isDoubleNum(Object l, Object r) {
        return l instanceof Double && r instanceof Double;
    }

    private boolean isEqual(Object l, Object r) {
        if (l == null && r == null) {
            return true;
        }
        if (l == null) {
            return false;
        }
        return l.equals(r);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperand(Token operator, Object left, Object right) {
        if (isDoubleNum(left, right)) {
            return;
        }
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private String stringify(Object object) {
        if (object == null) {
            return "nil";
        }

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private String transformToString(Object left, Object right) {
        String str;
        Object num;
        boolean stringFirst = false;

        if (left instanceof Double) {
            str = String.valueOf(left);
            num = right;
            stringFirst = true;
        }else{
            str = String.valueOf(right);
            num = left;
        }

        str = str.endsWith(".0") ? str.substring(0,str.length()-2) : str;
        return stringFirst ? str + num : num + str;
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    /**
     * Stmt的visit方法
     */

    private void execute(Stmt statement) {
        statement.accept(this);
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try{
            this.environment = environment;

            for (Stmt statement : statements) {
                if (isContinue) {
                    isContinue = false;
                    return;
                }
                if (isBreak) {
                    return;
                }
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Object superclass = null;
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);
            if (!(superclass instanceof LoxClass)) {
                throw new RuntimeError(stmt.superclass.name,
                        "Superclass must be a class.");
            }
        }

        environment.define(stmt.name.lexeme, null);

        if (stmt.superclass != null) {
            environment = new Environment(environment);
            environment.define("super", superclass);
        }

        Map<String, LoxFunction> methods = new HashMap<>(10);
        Map<String, LoxFunction> staticMethods = new HashMap<>(10);
        for (Stmt.Function method : stmt.methods) {
            LoxFunction function = new LoxFunction(method, environment,
                    "init".equals(method.name.lexeme));
            if ("static".equals(method.kind)) {
                staticMethods.put(method.name.lexeme, function);
                continue;
            }

            methods.put(method.name.lexeme, function);
        }

        LoxClass klass = new LoxClass(stmt.name.lexeme, (LoxClass)superclass, methods, staticMethods);

        if (superclass != null) {
            environment = environment.enclosing;
        }

        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment, false);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) {
            value = evaluate(stmt.value);
        }

        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition)) && !isBreak) {
            execute(stmt.body);
        }
        isBreak = false;
        return null;
    }

    /**************************************************************************/

}
