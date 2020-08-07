package com.craftinginterpreters.lox;

/**
 * @author Javen-Liu
 * @version 1.0
 * @date 2020/6/12 18:05
 * @github https://github.com/Javen-Liu
 */
public class Token {
    /**
     * 每个词汇的种类
     */
    final TokenType type;

    /**
     * 每个词汇的字面字符串表示
     * 例如对于 "str"，lexeme 为 "str";
     * 对于 = ，lexeme 为 =
     */
    final String lexeme;

    /**
     * 若不为文本字符串，则此项为null
     * 若为文本字符串，则此项为其具体内容
     * 例如对于 "str"，literal 为 str；
     * 对于 = ，literal 为 null
     */
    final Object literal;

    /**
     * 记录当前词汇所在的行
     */
    final int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString(){
        return type + " " + lexeme + " " + literal;
    }
}
