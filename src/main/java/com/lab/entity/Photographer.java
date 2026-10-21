package com.lab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 摄影师实体：与 photo_studio.photographer 表一一对应。
 * 说明 1：joinDate 使用 LocalDate（JDK8 时间 API），由 MyBatis 内置类型处理器直接支持。
 * 说明 2：实现 Serializable —— MyBatis 二级缓存以序列化方式保存数据，
 *        实体不可序列化会在写入二级缓存时报 NotSerializableException。
 */
@TableName("photographer")
public class Photographer implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 摄影师编号，数据库自增并回填 */
    @TableId(value = "photographer_id", type = IdType.AUTO)
    private Integer photographerId;

    /** 姓名 */
    @TableField("photographer_name")
    private String photographerName;

    /** 性别 */
    private String gender;

    /** 工作室 */
    private String studio;

    /** 擅长领域 */
    private String specialty;

    /** 日薪 */
    private BigDecimal dayRate;

    /** 入驻日期 */
    @TableField("join_date")
    private LocalDate joinDate;

    /** 状态：1 在职，0 离职 */
    private Integer status;

    /**
     * 该摄影师的作品列表（一对多）。
     * exist = false：数据库中没有这一列，仅由 {@code <collection>} 联表查询填充，
     * 避免 MP 在 insert / update 时把它当成真实字段拼进 SQL。
     */
    @TableField(exist = false)
    private List<PhotoWork> works;

    public Photographer() {
    }

    public Photographer(String photographerName, String gender, String studio, String specialty,
               BigDecimal dayRate, LocalDate joinDate, Integer status) {
        this.photographerName = photographerName;
        this.gender = gender;
        this.studio = studio;
        this.specialty = specialty;
        this.dayRate = dayRate;
        this.joinDate = joinDate;
        this.status = status;
    }

    public Integer getPhotographerId() {
        return photographerId;
    }

    public void setPhotographerId(Integer photographerId) {
        this.photographerId = photographerId;
    }

    public String getPhotographerName() {
        return photographerName;
    }

    public void setPhotographerName(String photographerName) {
        this.photographerName = photographerName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public BigDecimal getDayRate() {
        return dayRate;
    }

    public void setDayRate(BigDecimal dayRate) {
        this.dayRate = dayRate;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<PhotoWork> getWorks() {
        return works;
    }

    public void setWorks(List<PhotoWork> works) {
        this.works = works;
    }

    @Override
    public String toString() {
        return "Photographer{photographerId=" + photographerId
                + ", photographerName='" + photographerName + '\''
                + ", gender='" + gender + '\''
                + ", studio='" + studio + '\''
                + ", specialty='" + specialty + '\''
                + ", dayRate=" + dayRate
                + ", joinDate=" + joinDate
                + ", status=" + status
                + '}';
    }
}
