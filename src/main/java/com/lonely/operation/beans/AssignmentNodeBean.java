package com.lonely.operation.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author ztkj-hzb
 * @Date 2019/11/29 11:20
 * @Description 赋值节点实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentNodeBean {

    /**
     * 左侧表达式配置
     */
    private List<ParamBean> leftParamBeans;

    /**
     * 右侧表达式配置
     */
    private ExpressionValueBean expressionValueBean;

}
