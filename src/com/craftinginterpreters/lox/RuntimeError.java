package com.craftinginterpreters.lox;

/**
 * @author Javen-Liu
 * @version 1.0
 * @date 2020/6/26 17:36
 * @github https://github.com/Javen-Liu
 */
public class RuntimeError extends RuntimeException {
    final Token token;

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
