package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

/**
 * @author Javen-Liu
 * @version 1.0
 * @date 2020/6/12 18:19
 * @github https://github.com/Javen-Liu
 */
public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> KEYWORDS;

    static {
        KEYWORDS = new HashMap<>();
        KEYWORDS.put("and",      AND);
        KEYWORDS.put("class",    CLASS);
        KEYWORDS.put("continue", CONTINUE);
        KEYWORDS.put("break",    BREAK);
        KEYWORDS.put("else",     ELSE);
        KEYWORDS.put("false",    FALSE);
        KEYWORDS.put("for",      FOR);
        KEYWORDS.put("fun",      FUN);
        KEYWORDS.put("if",       IF);
        KEYWORDS.put("nil",      NIL);
        KEYWORDS.put("or",       OR);
        KEYWORDS.put("print",    PRINT);
        KEYWORDS.put("return",   RETURN);
        KEYWORDS.put("super",    SUPER);
        KEYWORDS.put("this",     THIS);
        KEYWORDS.put("true",     TRUE);
        KEYWORDS.put("var",      VAR);
        KEYWORDS.put("while",    WHILE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    /**
     * 扫描每一行的语句
     * @return 装有扫描完后的Token实体类对象的列表
     */
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    /**
     * 当前是否已经扫描完毕
     * @return 若扫描完，则返回true
     *         若未扫描完，则返回false
     */
    private boolean isAtEnd(){
        return current >= source.length();
    }

    /**
     * 对当前行的语句进行扫描
     */
    private void scanToken(){
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(match('-') ? MINUS_MINUS : MINUS); break;
            case '+': addToken(match('+') ? PLUS_PLUS : PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;

            // 判断如果连续两个/，即//，则表明该行为注释，可直接略去
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else if(match('*')){
                    comment();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t': break;
            case '\n':
                line++;
                break;

            // 以 " 开头的则为文本字符串
            case '"':
                string();
                break;

            // 若都没匹配上则最后判断是否为break、continue、数字 或者 字符，不是则抛出错误
            default:
                // if (isKeyword(c, String.valueOf(BREAK).toLowerCase())) {
                //     addToken(BREAK);
                // } else if (isKeyword(c, String.valueOf(CONTINUE).toLowerCase())) {
                //     addToken(CONTINUE);
                // } else if (isDigit(c)) {
                //     number();
                // } else if (isAlpha(c)) {
                //     identifier();
                // }else{
                //     Lox.error(line, "Unexpected character.");
                // }
                // break;

                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                }else{
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    /**
     * 如果当前字符扫描出来是非文本字符，将其添
     * 加入tokens列表时，literal参数传进null，
     * 表示该字符为非文本字符
     * @param type 传入当前字符的类型
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * 将当前字符添加到tokens列表中
     * @param type 传入当前字符的类型
     * @param literal 文本信息，如果字符为非文本字符，则为null
     */
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }

        // Unterminated string 未结束的字符串
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // 末尾的"
        advance();

        // 将文本字符串添加进tokens列表中
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    /**
     * 获得当前扫描位置的字符
     * @return 字符类型数值
     */
    private char advance() {
        return source.charAt(current++);
    }

    /**
     * 判断当前字符是否与传入的expected参数相同
     * @param expected 判断的目标字符
     * @return 若符合，则返回true
     *         若当前行的语句已经扫描完毕或者不符合，则返回false
     */
    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }

        if (source.charAt(current) != expected) {
            return false;
        }

        current++;
        return true;
    }

    /**
     * 和advance()方法很相似，但是该方法实际上
     * 只是查看当前current指针所指向的字符，并
     * 不对current进行操作
     * @return 返回当前current指针指向的字符
     */
    private char peek(){
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    /**
     * 查看当前指针的下一个位置上的字符数据
     * @return 返回当前current指针下一位置的字符
     */
    private char peekNext(){
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private void comment(){
        while (!isAtEnd() && ( peek() != '*' || peekNext() != '/' )) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Illegal comments");
            return;
        }

        advance();
        advance();
    }

    /**
     * 判断是否为数字
     * @param c 传入的字符参数
     * @return 如果是数字，则返回true
     *         如果不是数字，则返回false
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * 判断是否为字母
     * @param c 传入的字符参数
     * @return 如果是字母，则返回true
     *         如果不是字母，则返回false
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    /**
     * 判断是否为字母或数字
     * @param c 传入的字符参数
     * @return 如果是，则返回true
     *         如果不是，则返回false
     */
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /**
     * 向tokens列表中添加数字
     */
    private void number(){
        while (isDigit(peek())) {
            advance();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) {
                advance();
            }
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier(){
        while (isAlphaNumeric(peek())) {
            advance();
        }
        String text = source.substring(start, current);

        TokenType type = KEYWORDS.get(text);
        type = type == null ? IDENTIFIER : type;
        addToken(type);
    }

    private boolean isKeyword(char c, String keyword) {
        int point = current - 1;

        if (keyword.charAt(0) != c) {
            return false;
        }

        for (int i = 0; i < keyword.length(); i++) {
            if (source.charAt(point++) != keyword.charAt(i)) {
                return false;
            }
        }

        current = point;
        return true;
    }
}
