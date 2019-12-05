package com.lonely.operation.beans;

import org.apache.commons.lang3.StringUtils;

/**
 * @author ztkj-hzb
 * @Date 2019/9/24 10:20
 * @Description 参数实体
 */

public class ParamBean {

    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 参数类型
     */
    private String clazzName;

    /**
     * 是否是数组
     */
    private boolean isArray;

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getClazzName() {
        return clazzName;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean array) {
        isArray = array;
    }

    public ParamBean() {
    }

    public ParamBean(String paramName, String clazzName, boolean isArray) {
        this.paramName = paramName;
        this.clazzName = clazzName;
        this.isArray = isArray;
    }

    /**
     * 获取全类名对应的class对象
     *
     * @return
     */
    public Class getType() {
        if (StringUtils.isBlank(clazzName)) {
            return null;
        }
        try {
            return Class.forName(this.clazzName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
