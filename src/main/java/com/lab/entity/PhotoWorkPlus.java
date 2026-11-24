package com.lab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.util.Date;

/**
 * MyBatis-Plus 版实体：通过注解完成表/字段映射，无需 XML。
 */
@TableName("photo_work")
public class PhotoWorkPlus {
    @TableId(value = "work_id", type = IdType.AUTO)
    private Integer workId;

    @TableField("work_title")
    private String workTitle;

    @TableField("photographer")
    private String photographer;

    @TableField("shoot_type")
    private String shootType;

    @TableField("studio")
    private String studio;

    @TableField("shoot_date")
    private Date shootDate;

    @TableField("retouch_hours")
    private BigDecimal retouchHours;

    @TableField("price")
    private BigDecimal price;

    @TableField("status")
    private Integer status;

    public PhotoWorkPlus() {
    }

    public PhotoWorkPlus(String workTitle, String photographer, String shootType,
                         String studio, Date shootDate, BigDecimal retouchHours,
                         BigDecimal price, Integer status) {
        this.workTitle = workTitle;
        this.photographer = photographer;
        this.shootType = shootType;
        this.studio = studio;
        this.shootDate = shootDate;
        this.retouchHours = retouchHours;
        this.price = price;
        this.status = status;
    }

    public Integer getWorkId() {
        return workId;
    }

    public void setWorkId(Integer workId) {
        this.workId = workId;
    }

    public String getWorkTitle() {
        return workTitle;
    }

    public void setWorkTitle(String workTitle) {
        this.workTitle = workTitle;
    }

    public String getPhotographer() {
        return photographer;
    }

    public void setPhotographer(String photographer) {
        this.photographer = photographer;
    }

    public String getShootType() {
        return shootType;
    }

    public void setShootType(String shootType) {
        this.shootType = shootType;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public Date getShootDate() {
        return shootDate;
    }

    public void setShootDate(Date shootDate) {
        this.shootDate = shootDate;
    }

    public BigDecimal getRetouchHours() {
        return retouchHours;
    }

    public void setRetouchHours(BigDecimal retouchHours) {
        this.retouchHours = retouchHours;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "PhotoWorkPlus{workId=" + workId + ", workTitle='" + workTitle + '\''
                + ", photographer='" + photographer + '\''
                + ", shootType='" + shootType + '\''
                + ", studio='" + studio + '\''
                + ", shootDate=" + shootDate
                + ", retouchHours=" + retouchHours
                + ", price=" + price + ", status=" + status + '}';
    }
}
