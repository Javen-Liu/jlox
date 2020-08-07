package com.craftinginterpreters.lox;

/**
 * @author Javen-Liu
 * @version 1.0
 * @date 2020/7/23 17:01
 * @github https://github.com/Javen-Liu
 */
public class Return extends RuntimeException{
    final Object value;

    Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
