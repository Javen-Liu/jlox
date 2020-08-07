package com.craftinginterpreters.lox;

/**
 * @author Javen-Liu
 * @version 1.0
 * @date 2020/8/7 15:54
 * @github https://github.com/Javen-Liu
 */
public class LoxClass {
    final String name;

    LoxClass(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
