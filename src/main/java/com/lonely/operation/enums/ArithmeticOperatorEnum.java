package com.lonely.operation.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ztkj-hzb
 * @Date 2019/11/29 10:39
 * @Description 算术运算符枚举
 */
public enum ArithmeticOperatorEnum {

    /**
     * 加法
     */
    ADD("+"),
    /**
     * 减法
     */
    REDUCE("-"),
    /**
     * 乘法
     */
    MULTIPLICATION("*"),
    /**
     * 除法
     */
    DIVISION("/");

    /**
     * 操作符
     */
    public String operation;

    ArithmeticOperatorEnum(String operation) {
        this.operation = operation;
    }

    /**
     * 获取算术运算符操作集合
     *
     * @return
     */
    public static List<String> getOperations() {
        List<String> operations = new ArrayList<>();
        for (ArithmeticOperatorEnum arithmeticOperatorEnum : ArithmeticOperatorEnum.values()) {
            operations.add(arithmeticOperatorEnum.operation);
        }
        return operations;
    }

}
