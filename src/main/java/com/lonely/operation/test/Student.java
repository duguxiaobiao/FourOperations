package com.lonely.operation.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {

    /**
     * 姓名
     */
    private String name;

    /**
     * 成绩
     */
    private double score;

}