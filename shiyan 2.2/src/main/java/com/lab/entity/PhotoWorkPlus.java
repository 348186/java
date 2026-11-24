package com.lab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.util.Date;

/**
 * MyBatis-Plus 版实体：注解映射到 photo_work 表（摄影师以 photographer_id 关联）。
 */
@TableName("photo_work")
public class PhotoWorkPlus {
    @TableId(value = "work_id", type = IdType.AUTO)
    private Integer workId;

    @TableField("photographer_id")
    private Integer photographerId;

    @TableField("work_title")
    private String workTitle;

    @TableField("location")
    private String location;

    @TableField("shoot_date")
    private Date shootDate;

    @TableField("style")
    private String style;

    @TableField("price")
    private BigDecimal price;

    @TableField("status")
    private Integer status;

    public PhotoWorkPlus() {
    }

    public PhotoWorkPlus(Integer photographerId, String workTitle, String location,
                         Date shootDate, String style, BigDecimal price, Integer status) {
        this.photographerId = photographerId;
        this.workTitle = workTitle;
        this.location = location;
        this.shootDate = shootDate;
        this.style = style;
        this.price = price;
        this.status = status;
    }

    public Integer getWorkId() {
        return workId;
    }

    public void setWorkId(Integer workId) {
        this.workId = workId;
    }

    public Integer getPhotographerId() {
        return photographerId;
    }

    public void setPhotographerId(Integer photographerId) {
        this.photographerId = photographerId;
    }

    public String getWorkTitle() {
        return workTitle;
    }

    public void setWorkTitle(String workTitle) {
        this.workTitle = workTitle;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getShootDate() {
        return shootDate;
    }

    public void setShootDate(Date shootDate) {
        this.shootDate = shootDate;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
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
        return "PhotoWorkPlus{workId=" + workId
                + ", photographerId=" + photographerId
                + ", workTitle='" + workTitle + '\''
                + ", location='" + location + '\''
                + ", shootDate=" + shootDate
                + ", style='" + style + '\''
                + ", price=" + price + ", status=" + status + '}';
    }
}
