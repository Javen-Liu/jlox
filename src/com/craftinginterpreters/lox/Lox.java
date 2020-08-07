package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Javen-Liu
 * @version 1.0
 * @date 2020/6/12 14:52
 * @github https://github.com/Javen-Liu
 */
@SuppressWarnings("unused")
public class Lox {

    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;
    private static final Interpreter INTERPRETER = new Interpreter();
    private static boolean debug = false;

    /**
     * 启动lox语言
     * @param args 命令行参数
     * @throws IOException IO异常
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 2) {
            if ("debug".equals(args[0])) {
                debug = true;
                runFile(args[1]);
            } else if ("debug".equals(args[1])) {
                debug = true;
                runFile(args[0]);
            }
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }else if(args.length == 1){
            runFile(args[0]);
        }else{
            runPrompt();
        }
    }

    /**
     * 若传入命令行的数据为字符串，即表示打开指定路径的文件并进行扫描
     * @param path 路径的字符串表示
     * @throws IOException IO异常
     */
    private static void runFile(String path) throws IOException{
        // 指示退出代码中的错误
        if(hadError){
            System.exit(65);
        }
        if (hadRuntimeError) {
            System.exit(70);
        }

        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    /**
     * 若无命令函参数，则执行即时编译
     * @throws IOException IO异常
     */
    private static void runPrompt() throws IOException{
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while(true){
            System.out.print("> ");
            run(reader.readLine());
            hadError = false;
        }
    }

    /**
     * 对输入的语句进行扫描并封装为Token
     * @param source 输入的语句
     */
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        if (debug) {
            for (Token token : tokens) {
                System.out.println(token);
            }
        }

        // 对当前扫描出的token进行操作
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // 如果发生错误，则停止程序
        if (hadError) {
            return;
        }

        Resolver resolver = new Resolver(INTERPRETER);
        resolver.resolve(statements);

        INTERPRETER.interpret(statements);
    }

    /**
     * 当发生错误时，进行错误处理
     * @param line 发生错误的行
     * @param message 错误信息
     */
    static void error(int line, String message) {
        report(line, "", message);
    }

    /**
     * 当发生错误时，进行错误处理
     * @param line 发生错误的行
     * @param where 发生错误的词汇
     * @param message 错误信息
     */
    static void error(int line, String where, String message) {
        report(line, where, message);
    }

    /**
     * 当发生错误时，进行错误处理
     * @param token Token类对象
     * @param message 错误信息
     */
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, "at end", message);
        }else{
            report(token.line, "at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " +
                error.token.line + "]");
        hadRuntimeError = true;
    }

    /**
     * 控制台打印错误信息
     * @param line 发生错误的行
     * @param where 具体位置
     * @param message 错误信息
     */
    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error " +
                where + ": " + message);
    }
}
