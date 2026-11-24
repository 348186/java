package com.lab.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 摄影作品实体，对应 photo_work 表。
 * 采用 Date/BigDecimal 包装类型，便于映射与空值处理。
 */
public class PhotoWork {
    private Integer workId;
    private String workTitle;
    private String photographer;
    private String shootType;
    private String studio;
    private Date shootDate;
    private BigDecimal retouchHours;
    private BigDecimal price;
    private Integer status;

    public PhotoWork() {
    }

    public PhotoWork(String workTitle, String photographer, String shootType,
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
        return "PhotoWork{workId=" + workId + ", workTitle='" + workTitle + '\''
                + ", photographer='" + photographer + '\''
                + ", shootType='" + shootType + '\''
                + ", studio='" + studio + '\''
                + ", shootDate=" + shootDate
                + ", retouchHours=" + retouchHours
                + ", price=" + price + ", status=" + status + '}';
    }
}
