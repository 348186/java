package com.lab.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 多表联查视图对象：作品 + 摄影师信息（photo_work INNER/LEFT JOIN photographer）。
 *
 * <p>为什么用 VO 而不是直接返回实体：联查结果横跨两张表，
 * 实体里既没有 photographerName 也没有 studio，直接映射会丢字段，
 * 因此需要专门的结果对象承载。
 */
public class WorkDetailVO {

    // ---- 来自 photo_work ----
    private Integer workId;
    private String workTitle;
    private String location;
    private LocalDate shootDate;
    private String style;
    private BigDecimal price;
    private Integer workStatus;

    // ---- 来自 photographer ----
    private Integer photographerId;
    private String photographerName;
    private String studio;
    private String specialty;

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getShootDate() {
        return shootDate;
    }

    public void setShootDate(LocalDate shootDate) {
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

    public Integer getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(Integer workStatus) {
        this.workStatus = workStatus;
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

    @Override
    public String toString() {
        return "WorkDetailVO{workId=" + workId
                + ", workTitle='" + workTitle + '\''
                + ", style='" + style + '\''
                + ", price=" + price
                + ", photographer='" + photographerName + '\''
                + ", studio='" + studio + '\''
                + ", location='" + location + '\''
                + '}';
    }
}
