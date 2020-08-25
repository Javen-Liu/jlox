package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

/**
 * @author Javen-Liu
 * @version 1.0
 * @date 2020/8/7 15:54
 * @github https://github.com/Javen-Liu
 */
public class LoxClass implements LoxCallable{
    final String name;
    final LoxClass superclass;
    private final Map<String, LoxFunction> methods;
    private final Map<String, LoxFunction> staticMethods;

    LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods, Map<String, LoxFunction> staticMethods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
        this.staticMethods = staticMethods;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if (initializer == null) {
            return 0;
        }
        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superclass != null) {
            return superclass.findMethod(name);
        }

        return null;
    }

    LoxFunction findStaticMethod(String name) {
        if (staticMethods.containsKey(name)) {
            return staticMethods.get(name);
        }

        return null;
    }
}
