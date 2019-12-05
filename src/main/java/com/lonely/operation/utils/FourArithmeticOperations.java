package com.lonely.operation.utils;

import com.graphbuilder.math.ExpressionTree;
import com.lonely.operation.beans.ExpressionValueBean;
import com.lonely.operation.beans.ParamBean;
import com.lonely.operation.enums.ArithmeticOperatorEnum;
import com.lonely.operation.test.Student;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author ztkj-hzb
 * @Date 2019/11/28 14:07
 * @Description 四则运算工具类, 这里只处理 非以{xx.xx}格式的数据，因为这种数据直接DefaultAssignemntUtil工具类即可，本工具类是必须直接计算出右侧值的这种
 * 思路：
 * 1.将{}中包括的内容先处理，然后再次压入栈中，构建出最终的算术表达式
 * 2.借助JavaScript脚本语言特性，eval()方法执行得到最终的结果
 * <p>
 * 问题：
 * 1. 假如在计算 1+{xx.xx} 的过程中 表达式结果为null时该怎么处理,即拿不到表达式结果或表达式结果为null的情况
 */
public class FourArithmeticOperations {


    private static final Logger logger = LoggerFactory.getLogger(FourArithmeticOperations.class);

    /**
     * 表达式队列  即例如： 1+2+{xxx.xxx}+3
     */
    private static Queue<String> expressionQueue;


    /**
     * 临时表达式计算队列 即例如 xxx.xxx 计算结果，将结果压入表达式队列中
     */
    private static Queue<String> tempCalcQueue;


    /**
     * 表达式参数配置集合
     */
    private static Queue<List<ParamBean>> expressionParamConfigurations;

    /**
     * 四则运算符集合
     */
    private static List<String> operators = ArithmeticOperatorEnum.getOperations();

    /**
     * js脚本引擎对象,但是在docker环境中会导致该对象为null，则可以使用另一种方法 ExpressionTree类使用
     */
    private static ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");

    private static Student student = new Student("lonely", 26);


    public static void main(String[] args) {

        ExpressionValueBean expressionValueBean = new ExpressionValueBean();
        Queue<List<ParamBean>> paramQueue = new ConcurrentLinkedQueue<>();
        paramQueue.add(new ArrayList<ParamBean>(){{
            this.add(new ParamBean("score","java.lang.Double",false));
        }});

        Queue<List<ParamBean>> newQueue = new ConcurrentLinkedQueue<>(paramQueue);

        expressionValueBean.setRightParamBeans(paramQueue);
        expressionValueBean.setRightFieldValueExpression("1+2+3+{score}+1");
        analysisAndCalculationProcess(student,expressionValueBean);

        /*analysisAndCalculationProcess(student,"1+2+3+{score}");
        analysisAndCalculationProcess(student,"1+2");
        analysisAndCalculationProcess(student,"1+2+{name}");
        analysisAndCalculationProcess(student,"1+2*{name}");*/
    }

    /**
     * 解析表达式，并返回最终的结果
     * @param object
     * @param expressionValueBean
     */
    public static Object analysisAndCalculationProcess(Object object, ExpressionValueBean expressionValueBean) {
        if (StringUtils.isEmpty(expressionValueBean.getRightFieldValueExpression())) {
            return null;
        }

        //分析表达式，存入到表达式队列中
        analysisProcess(object,expressionValueBean);

        //计算最终表达式的结果
        return calculationProcess();
    }


    /**
     * 分析过程，将指定表达式拆分到对应的栈中的过程
     * todo 注意：这里已知知道数据格式不存在括号嵌套的情况，即不存在{xxx.{xxxx.xx}}，只会{xxx.xxx}+{fdaf.fda}等格式
     *
     * @param object
     * @param expressionValueBean
     */
    private static void analysisProcess(Object object,ExpressionValueBean expressionValueBean) {

        expressionQueue = new ConcurrentLinkedQueue<>();
        tempCalcQueue = new ConcurrentLinkedQueue<>();
        expressionParamConfigurations = expressionValueBean.getRightParamBeans();

        String expression = expressionValueBean.getRightFieldValueExpression();

        for (int i = 0; i < expression.length(); i++) {
            String currChar = String.valueOf(expression.charAt(i));

            switch (currChar) {
                case "{":
                    //特殊表达式开始，放入临时表达式计算队列中
                    tempCalcQueue.add(currChar);
                    break;
                case "}":
                    //特殊表达式结束，计算临时表达式数据
                    Object result = operationCalcProcess(object);
                    if (result == null) {
                        //计算结果为空，先抛出
                        throw new RuntimeException("当前表达式结果为空");
                    }
                    //校验并获取表达式后 放入表达式队列中
                    expressionQueue.add(checkResultAndReturnString(result, expression));
                    break;
                default:
                    //普通字符，判断临时表达式计算中是否存在元素，存在，则进入计算队列中
                    if (!tempCalcQueue.isEmpty()) {
                        tempCalcQueue.add(currChar);
                    } else {
                        // 直接压入表达式队列
                        expressionQueue.add(currChar);
                    }
                    break;
            }
        }

    }

    /**
     * 最终表达式计算过程
     *
     * @return
     */
    private static Object calculationProcess() {

        //从队列中获取最终的表达式信息
        String finalExpression = String.join("", expressionQueue);
        if (StringUtils.isEmpty(finalExpression)) {
            throw new RuntimeException("最终构架的表达式为空，无法计算，请检查");
        }

        //最终的结果
        Object eval = null;

        //判断最终的表达式是需要计算的还是固定值
        boolean needEval = Stream.of(finalExpression.split("")).anyMatch(x -> operators.contains(x));
        if (needEval) {
            //存在四则运算符，需要计算

            //计算
            try {
                //第一种,在docker环境下不行
                //eval = scriptEngine.eval(finalExpression);

                //第二种，都可以
                eval = ExpressionTree.parse(finalExpression).eval(null, null);
            } catch (Exception e) {
                logger.error("计算指定表达式：{}出现异常，异常原因：{}", finalExpression, ExceptionUtils.getStackTrace(e));
            }
        } else {
            //不需要计算，最终的结果就是表达式
            eval = finalExpression;
        }

        System.out.println(MessageFormat.format("计算指定表达式：{0}的结果：{1}", finalExpression, eval));
        return eval;

    }


    /**
     * 表达式计算过程
     *
     * @return
     */
    private static Object operationCalcProcess(Object object) {
        if (tempCalcQueue == null || tempCalcQueue.isEmpty()) {
            throw new RuntimeException("临时表达式计算队列中没有需要计算的表达式");
        }
        String calcExpression = String.join("", tempCalcQueue).replace("{", "").replace("}", "");

        //获取当前的表达式对应的类型配置信息
        List<ParamBean> currParamConfigs = expressionParamConfigurations.remove();

        Object result = DefaultAssignmentSubstitutionUtil.getExpressionResults(object,currParamConfigs);

        //计算完后，清空临时队列
        tempCalcQueue.clear();
        return result;
    }


    /**
     * 校验结果类型是否满足表达式格式，且返回正确的表达式字符串
     *
     * @param result
     * @param expression
     * @return
     */
    private static String checkResultAndReturnString(Object result, String expression) {
        if (ClassUtil.isWrapClass(result.getClass())) {
            //基础数据类型
            return String.valueOf(result);
        } else {
            //引用类型
            if (result instanceof String) {
                //字符串类型，则表达式中只允许存在 + 法运算
                boolean anyMatch = Stream.of(expression.split("")).filter(x -> !x.equalsIgnoreCase(ArithmeticOperatorEnum.ADD.operation))
                        .anyMatch(x -> operators.contains(x));
                if (anyMatch) {
                    throw new RuntimeException("针对存在字符串类型的数据结果，不支持除了+号之外的运算");
                } else {
                    //最多只存在 + 号运算符
                    return MessageFormat.format("\"{0}\"", result);
                }
            } else {
                //其他对象类型，不支持四则运算
                throw new RuntimeException("针对存在对象类型的数据结果，不支持四则运算");
            }
        }
    }

    /**
     * 校验该结果能否符合表达式的格式
     *
     * @param result
     * @param expression
     * @return
     */
    /*private static boolean checkResultEffectiveness(Object result, String expression) {
        //判断表达式中是否只有一段赋值，还是组合赋值
        int size = countNumberOfCharacters(expression);
        if (size == 0) {
            //没有需要计算的表达式
            return true;
        }

        if (ClassUtil.isWrapClass(result.getClass())) {
            //基础数据类型
            return true;
        } else {
            //引用类型

            //判断是否只有一个表达式待处理
            if (size == 1 && expression.startsWith("{")) {
                //当前待解析的表达式中只有一个表达式需要解析，且以{开头，则说明该表达式没有后续的四则运算，则直接返回
                return true;
            }

            if (result instanceof String) {
                //字符串类型，则表达式中只允许存在 + 法运算
                boolean anyMatch = Stream.of(expression.split("")).filter(x -> !x.equalsIgnoreCase("+"))
                        .anyMatch(x -> operators.contains(x));
                if (anyMatch) {
                    throw new RuntimeException("针对存在字符串类型的数据结果，不支持除了+号之外的运算");
                }
            } else {
                //其他对象类型，不支持四则运算
                throw new RuntimeException("针对存在对象类型的数据结果，不支持四则运算");
            }
        }
        return true;
    }*/
    public static int countNumberOfCharacters(String expression) {
        //正则表达式 匹配{xx.xx}有多少个
        //String rexp = "\\{[\\w\\.]*\\}";
        String rexp = "\\{[\\u4e00-\\u9fa5_a-zA-Z0-9\\.]*\\}";

        int count = 0;
        Pattern pattern = Pattern.compile(rexp);
        Matcher matcher = pattern.matcher(expression);
        while (matcher.find()) {
            System.out.println(expression.substring(matcher.start(),matcher.end()));
            count++;
        }
        return count;
    }

}

