package com.lonely.operation.test;

import com.lonely.operation.beans.AssignmentNodeBean;
import com.lonely.operation.beans.ExpressionValueBean;
import com.lonely.operation.beans.ParamBean;
import com.lonely.operation.utils.DefaultAssignmentSubstitutionUtil;
import com.lonely.operation.utils.FourArithmeticOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author ztkj-hzb
 * @Date 2019/11/29 11:34
 * @Description 测试固定值赋值
 */
public class TestFleuxAssignment {


    public static void main(String[] args) {


        String a = "{输出对象.阿萨.啊}+123+{输入对象.测试.啊}+{啥时.啊}" +"a.b" +"a1.2";
        //String a = "{a.b}+123+{a.c}+{a.b.c}";

        System.out.println(FourArithmeticOperations.countNumberOfCharacters(a));



        if(true){
            return;
        }


        Student student = new Student("lonely", 26);

        List<AssignmentNodeBean> assignmentNodeBeans = new ArrayList<>();
        AssignmentNodeBean assignmentNodeBean = new AssignmentNodeBean();
        assignmentNodeBean.setLeftParamBeans(new ArrayList<ParamBean>(){{
            this.add(new ParamBean("score",Double.class.getName(),false));
        }});

        ExpressionValueBean expressionValueBean = new ExpressionValueBean();
        Queue<List<ParamBean>> queue = new ConcurrentLinkedQueue<>();
        List<ParamBean> paramBeans = new ArrayList<ParamBean>(){{
            this.add(new ParamBean("score",Double.class.getName(),false));
        }};
        queue.add(paramBeans);

        expressionValueBean.setRightParamBeans(queue);
        expressionValueBean.setRightFieldValueExpression("1+2+3+{score}");

        assignmentNodeBean.setExpressionValueBean(expressionValueBean);

        assignmentNodeBeans.add(assignmentNodeBean);


        DefaultAssignmentSubstitutionUtil.assignmentSubstitutionOfFixedValue(student,assignmentNodeBeans);

        System.out.println(student);


    }



}
