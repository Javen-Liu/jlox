package com.craftinginterpreters.lox;

import java.util.List;

/**
 * @author Javen-Liu
 * @version 1.0
 * @date 2020/7/23 15:12
 * @github https://github.com/Javen-Liu
 */
public class LoxFunction implements LoxCallable{
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);

        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }

        if (isInitializer) {
            return closure.getAt(0, "this");
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration + ">";
    }

    LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment, isInitializer);
    }
}
