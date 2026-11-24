package com.lab.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 摄影作品实体，对应 photo_work 表。
 * photographerName 为联表查询得到的冗余展示字段，不对应 photo_work 列。
 */
public class PhotoWork {
    private Integer workId;
    private Integer photographerId;
    private String workTitle;
    private String location;
    private Date shootDate;
    private String style;
    private BigDecimal price;
    private Integer status;
    private String photographerName;

    public PhotoWork() {
    }

    public PhotoWork(Integer photographerId, String workTitle, String location,
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

    public String getPhotographerName() {
        return photographerName;
    }

    public void setPhotographerName(String photographerName) {
        this.photographerName = photographerName;
    }

    @Override
    public String toString() {
        return "PhotoWork{workId=" + workId
                + ", photographerId=" + photographerId
                + ", workTitle='" + workTitle + '\''
                + ", location='" + location + '\''
                + ", shootDate=" + shootDate
                + ", style='" + style + '\''
                + ", price=" + price
                + ", status=" + status
                + (photographerName == null ? "" : ", photographerName='" + photographerName + '\'')
                + '}';
    }
}
