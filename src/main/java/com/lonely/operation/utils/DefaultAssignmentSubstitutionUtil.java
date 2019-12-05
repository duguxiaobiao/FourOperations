package com.lonely.operation.utils;


import com.lonely.operation.beans.AssignmentNodeBean;
import com.lonely.operation.beans.ParamBean;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ztkj-hzb
 * @Date 2019/9/24 10:14
 * @Description 默认的赋值替换的工具类，根据配置，将右侧的数据 赋值 给 左侧数据
 */
public class DefaultAssignmentSubstitutionUtil {


    private DefaultAssignmentSubstitutionUtil() {
    }

    /**
     * 获取指定表达式的结果，例如有一个临时变量  String a = 响应结果.用户.用户名,  需要返回 响应结果.用户.用户名 的结果操作
     *
     * @param objInstance
     * @param paramBeans
     * @return
     */
    public static Object getExpressionResults(Object objInstance, List<ParamBean> paramBeans) {

        //循环构建表达式
        if (CollectionUtils.isEmpty(paramBeans)) {
            return null;
        }


        //1.根据参数构建初级表达式，比如构建的表达式是： user[L0].student[L1].name
        String expression = getExpression(paramBeans);

        //2.根据值替换表达式
        if (StringUtils.isEmpty(expression)) {
            return null;
        }
        Set<String> expressSet = new LinkedHashSet<>();
        expressSet.add(expression);
        //将处理表达式根据对象值来构建成赋值表达式
        Set<String> replaceExpression = replaceExpression(expressSet, 0, objInstance);

        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(objInstance);

        //判断是单条数据还是多条数据
        if (CollectionUtils.isEmpty(replaceExpression)) {
            throw new RuntimeException("玩个鸡儿，撒都米有了");
        }

        if (replaceExpression.size() == 1) {
            //只有一条，不要将结果累计，直接返回
            String resultExpressStr = replaceExpression.stream().findFirst().orElseThrow(() -> new RuntimeException("没有表达式，无法处理"));
            Object propertyValue = beanWrapper.getPropertyValue(resultExpressStr);
            return propertyValue;
        } else {
            //多条，需要将结果累计
            List<Object> result = new ArrayList<>();
            for (String resultExpressStr : replaceExpression) {
                Object propertyValue = beanWrapper.getPropertyValue(resultExpressStr);
                result.add(propertyValue);
            }

            //这里需要注意，如果最终需要返回的是一个数组，则需要将list转数组
            return result;
        }

    }


    /**
     * 将表达式构建成具体的赋值表达式，例如  student[L0].studentName -> student[0].studentName 和 student[1].studentName
     *
     * @param expressSet
     * @param currIndex
     * @param objInstance
     * @return
     */
    private static Set<String> replaceExpression(Set<String> expressSet, int currIndex, Object objInstance) {

        if (CollectionUtils.isEmpty(expressSet)) {
            return new LinkedHashSet<>();
        }

        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(objInstance);

        Set<String> resultSet = new LinkedHashSet<>();

        for (String express : expressSet) {
            if (StringUtil.regularMatchingListOrArrayFormat(express)) {
                //判断是集合还是数组
                String listReplaceStr = MessageFormat.format("[L{0}]", currIndex);
                String arrayReplaceStr = MessageFormat.format("[A{0}]", currIndex);

                //判断当前最近处理的是 list还是数组
                if (express.indexOf(listReplaceStr) > express.indexOf(arrayReplaceStr)) {
                    //先处理list
                    String subExpress = StringUtil.subStringListFormat(express, listReplaceStr);
                    List list = (List) beanWrapper.getPropertyValue(subExpress);
                    if (CollectionUtils.isEmpty(list)) {
                        return new LinkedHashSet<>();
                    }
                    for (int i = 0; i < list.size(); i++) {
                        String newExpress = express.replace(StringUtil.removeLeftAndRightMiddleBrackets(listReplaceStr), i + "");
                        resultSet.add(newExpress);
                    }
                } else {
                    //处理数组
                    String subExpress = StringUtil.subStringListFormat(express, arrayReplaceStr);
                    Object[] arrObj = (Object[]) beanWrapper.getPropertyValue(subExpress);
                    if (arrObj == null || arrObj.length == 0) {
                        return new LinkedHashSet<>();
                    }
                    for (int i = 0; i < arrObj.length; i++) {
                        String newExpress = express.replace(StringUtil.removeLeftAndRightMiddleBrackets(arrayReplaceStr), i + "");
                        resultSet.add(newExpress);
                    }
                }
            }

        }

        if (CollectionUtils.isEmpty(resultSet)) {
            return expressSet;
        }

        return replaceExpression(resultSet, ++currIndex, objInstance);
    }


    /**
     * 构建初级表达式
     *
     * @param paramBeans
     * @return
     */
    public static String getExpression(List<ParamBean> paramBeans) {

        //循环构建表达式
        if (CollectionUtils.isEmpty(paramBeans)) {
            return StringUtils.EMPTY;
        }

        String prefix = StringUtils.EMPTY;
        int index = 0;
        for (int i = 0; i < paramBeans.size(); i++) {
            ParamBean paramBean = paramBeans.get(i);
            //判断是否是集合类型
            if (ClassUtil.isListTypeClass(paramBean.getType())) {
                //集合类型，先获取目前为止的集合的值
                prefix = StringUtils.isEmpty(prefix) ? MessageFormat.format("{0}[L{1}]", paramBean.getParamName(), index++) :
                        MessageFormat.format("{0}.{1}[L{2}]", prefix, paramBean.getParamName(), index++);
            } else if (paramBean.isArray()) {
                //数组类型
                prefix = StringUtils.isEmpty(prefix) ? MessageFormat.format("{0}[A{1}]", paramBean.getParamName(), index++) :
                        MessageFormat.format("{0}.{1}[A{2}]", prefix, paramBean.getParamName(), index++);
            } else {
                //非集合类型
                prefix = StringUtils.isEmpty(prefix) ? paramBean.getParamName() : MessageFormat.format("{0}.{1}", prefix, paramBean.getParamName());
            }
        }

        return prefix;
    }

    /*public static void getValue(Object obj,List<ParamBean> lefetParamBeans,Object rightValue){*/

    /**
     * 针对右侧表达式中存在固定值场景，给左侧表达式依赖的对象赋值
     * @param obj
     * @param assignmentBeans
     */
    public static void assignmentSubstitutionOfFixedValue(Object obj, List<AssignmentNodeBean> assignmentBeans) {

        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(obj);
        //todo 这句话很重要，取值可以不用设置，但是赋值一定需要，因为他的作用是在对象属性为null时，会帮你new一个，避免了空指针异常
        beanWrapper.setAutoGrowNestedPaths(true);

        if (CollectionUtils.isEmpty(assignmentBeans)) {
            return;
        }

        for (AssignmentNodeBean assignmentBean : assignmentBeans) {

            List<ParamBean> lefetParamBeans = assignmentBean.getLeftParamBeans();

            //获取右侧固定值
            Object rightValue = FourArithmeticOperations.analysisAndCalculationProcess(obj,assignmentBean.getExpressionValueBean());

            //判断当前表达式是否包含了集合
            long count = lefetParamBeans.stream().filter(paramBean -> ClassUtil.isListTypeClass(paramBean.getType()) || paramBean.isArray())
                    .count();

            if (count == 0) {
                //表达式
                String express = getExpression(lefetParamBeans);
                //3.设置属性
                beanWrapper.setPropertyValue(express, rightValue);
            } else if (count == 1) {
                //存在集合
                List list = new ArrayList();
                list.add(rightValue);

                String express = getExpression(lefetParamBeans);

                //判断先处理集合还是数组
                String listReplaceStr = MessageFormat.format("[L{0}]", 0);
                String arrayReplaceStr = MessageFormat.format("[A{0}]", 0);

                //判断是处理的是 list还是数组，这里不支持嵌套,所以只会存在一种
                String finalHandlerReplaceStr = express.contains(listReplaceStr) ? listReplaceStr : arrayReplaceStr;

                //替换表达式赋值
                for (int i = 0; i < list.size(); i++) {
                    String newExpress = express.replace(StringUtil.removeLeftAndRightMiddleBrackets(finalHandlerReplaceStr), i + "");
                    beanWrapper.setPropertyValue(newExpress, list.get(i));
                }
            } else {
                throw new RuntimeException("不支持嵌套list,请检查");
            }


        }


    }

}
