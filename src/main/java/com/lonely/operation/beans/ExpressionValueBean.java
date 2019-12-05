package com.lonely.operation.beans;


import java.util.List;
import java.util.Queue;

/**
 * @author ztkj-hzb
 * @Date 2019/9/24 10:22
 * @Description 用于表示赋值的表达式bean,该类最终表示 前端的一条展示 例如 输入对象.用户名 = 响应结果.用户.用户名 这个格式
 */
public class ExpressionValueBean {

    public ExpressionValueBean(){};

    /**
     * 右侧赋值信息
     */
    private Queue<List<ParamBean>> rightParamBeans;

    /**
     * 右侧固定值情况，右侧表达式
     */
    private String rightFieldValueExpression;

    public ExpressionValueBean(Queue<List<ParamBean>> rightParamBeans, String rightFieldValueExpression) {
        this.rightParamBeans = rightParamBeans;
        this.rightFieldValueExpression = rightFieldValueExpression;
    }

    public Queue<List<ParamBean>> getRightParamBeans() {
        return rightParamBeans;
    }

    public void setRightParamBeans(Queue<List<ParamBean>> rightParamBeans) {
        this.rightParamBeans = rightParamBeans;
    }

    public String getRightFieldValueExpression() {
        return rightFieldValueExpression;
    }

    public void setRightFieldValueExpression(String rightFieldValueExpression) {
        this.rightFieldValueExpression = rightFieldValueExpression;
    }
}
