package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Javen-Liu
 * @version 1.0
 * @date 2020/8/7 17:43
 * @github https://github.com/Javen-Liu
 */
public class LoxInstance {
    private LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }

    Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        LoxFunction method = klass.findMethod(name.lexeme);
        if (method != null) {
            return method.bind(this);
        }

        method = klass.findStaticMethod(name.lexeme);
        if (method != null) {
            throw new RuntimeError(name, "Instance cannot call static method.");
        }

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }
}
