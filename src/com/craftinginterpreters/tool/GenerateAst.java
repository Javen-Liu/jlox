package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * @author Javen-Liu
 * @version 1.0
 * @date 2020/6/16 17:45
 * @github https://github.com/Javen-Liu
 */
public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign   : Token name, Expr value",
                "Binary   : Expr left, Token operator, Expr right",
                "Call     : Expr callee, Token paren, List<Expr> arguments",
                "Get      : Expr object, Token name",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Logic    : Expr left, Token operator, Expr right",
                "Set      : Expr object, Token name, Expr value",
                "This     : Token keyword",
                "Unary    : Token operator, Expr right",
                "Variable : Token name",
                "Keyword  : Token name"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Block      : List<Stmt> statements",
                "Class      : Token name, List<Stmt.Function> methods",
                "Expression : Expr expression",
                "Function   : Token name, List<Token> params, List<Stmt> body",
                "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "Print      : Expr expression",
                "Return     : Token keyword, Expr value",
                "Var        : Token name, Expr initializer",
                "While      : Expr condition, Stmt body"
        ));
    }

    /**
     * 创建抽象语法数的脚本代码
     * @param outputDir 输出目录
     * @param baseName 父抽象类名称：Expr
     * @param types 继承父类的子类名称：Binary，Grouping，Literal，Unary
     * @throws IOException IO异常
     */
    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");
        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + "{");

        // 为产生的脚本文件的accept()方法添加注释
        writer.println("    /**");
        writer.println("     * 定义visitor模式中的accept()方法");
        writer.println("     * @param visitor Visitor类实例对象");
        writer.println("     * @param <R> 传入的泛型");
        writer.println("     * @return 泛型R的实例对象");
        writer.println("     */");

        // 访问者模式的accept()方法
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");
        writer.println();

        defineVisitor(writer, baseName, types);

        // 抽象数的类classes
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
            writer.println();
        }

        writer.println("}");
        writer.close();
    }

    /**
     * 为每个子类生成对应的变量和方法
     * @param writer 输出写出流
     * @param baseName 父抽象类名称：Expr
     * @param className 子类的名称
     * @param fieldList 子类中的成员变量
     */
    private static void defineType(PrintWriter writer, String baseName,
                                   String className, String fieldList) {
        writer.println("    static class " + className + " extends " + baseName + "{");
        String[] fields = fieldList.split(", ");

        // 变量
        for (String field : fields) {
            writer.println("        final " + field + ";");
        }
        writer.println();

        // 创建constructor
        writer.println("        " + className + "(" + fieldList + ") {");
        // 储存变量
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }
        writer.println("        }");

        // visitor模式
        writer.println();
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");

        writer.println("    }");
    }

    /**
     * 添加visitor设计模式的接口
     * @param writer 输出写出流
     * @param baseName 父抽象类名称：Expr
     * @param types 继承父类的子类名称：Binary，Grouping，Literal，Unary
     */
    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
        writer.println();
    }
}
