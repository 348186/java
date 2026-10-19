package com.lab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 员工实体：与 ssm_emp.emp 表一一对应。
 * 说明 1：hireDate 使用 LocalDate（JDK8 时间 API），由 MyBatis 内置类型处理器直接支持。
 * 说明 2：实现 Serializable —— MyBatis 二级缓存以序列化方式保存数据，
 *        实体不可序列化会在写入二级缓存时报 NotSerializableException。
 */
@TableName("emp")
public class Emp implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 员工编号，数据库自增并回填 */
    @TableId(value = "emp_id", type = IdType.AUTO)
    private Integer empId;

    /** 姓名 */
    @NotBlank(message = "员工姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过 50 个字符")
    @TableField("emp_name")
    private String empName;

    /** 性别 */
    @Pattern(regexp = "^[男女]$", message = "性别只能取值：男 / 女")
    private String gender;

    /** 部门 */
    private String dept;

    /** 岗位 */
    private String post;

    /** 薪资 */
    @NotNull(message = "薪资不能为空")
    @DecimalMin(value = "0", message = "薪资不能为负数")
    private BigDecimal salary;

    /** 入职时间 */
    @TableField("hire_date")
    private LocalDate hireDate;

    /** 状态：1 在职，0 离职 */
    private Integer status;

    public Emp() {
    }

    public Emp(String empName, String gender, String dept, String post,
               BigDecimal salary, LocalDate hireDate, Integer status) {
        this.empName = empName;
        this.gender = gender;
        this.dept = dept;
        this.post = post;
        this.salary = salary;
        this.hireDate = hireDate;
        this.status = status;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
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

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Emp{empId=" + empId
                + ", empName='" + empName + '\''
                + ", gender='" + gender + '\''
                + ", dept='" + dept + '\''
                + ", post='" + post + '\''
                + ", salary=" + salary
                + ", hireDate=" + hireDate
                + ", status=" + status
                + '}';
    }
}
