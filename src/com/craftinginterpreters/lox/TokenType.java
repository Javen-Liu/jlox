package com.craftinginterpreters.lox;

/**
 * @author Javen-Liu
 * @version 1.0
 * @date 2020/6/12 17:50
 * @github https://github.com/Javen-Liu
 */
public enum TokenType {

    // 单字符的标记
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // 双目运算符的标记
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // 具体文本字符
    IDENTIFIER, STRING, NUMBER,

    // 关键字
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,STATIC,

    // ++a, --a, a++, a--的操作符
    PLUS_PLUS,MINUS_MINUS,

    // break和continue操作
    BREAK,CONTINUE,

    EOF
}
