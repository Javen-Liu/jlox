package com.craftinginterpreters.lox;

import java.util.List;

/**
 * @author Javen-Liu
 * @version 1.0
 * @date 2020/7/22 19:49
 * @github https://github.com/Javen-Liu
 */
public interface LoxCallable {
    /**
     * 获取该调用方法的参数数量值
     * @return 参数数量值
     */
    int arity();

    /**
     * 方法进行调用
     * @param interpreter 解释器类实例对象
     * @param arguments   参数列表
     * @return 执行结果
     */
    Object call(Interpreter interpreter, List<Object> arguments);
}
