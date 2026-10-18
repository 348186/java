package com.lab.emp.dto;

import java.math.BigDecimal;

/**
 * 员工查询条件传输对象。
 * 与实体分离的好处：实体字段变动不会污染查询接口，也不必给查询参数加校验注解。
 */
public class EmpQueryDTO {

    /** 姓名（模糊） */
    private String name;

    /** 性别（精确） */
    private String gender;

    /** 部门（精确） */
    private String dept;

    /** 薪资下限 */
    private BigDecimal minSalary;

    /** 在职状态：1 在职，0 离职 */
    private Integer status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public BigDecimal getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(BigDecimal minSalary) {
        this.minSalary = minSalary;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
