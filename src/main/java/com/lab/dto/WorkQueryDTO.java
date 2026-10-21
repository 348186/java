package com.lab.dto;

import java.math.BigDecimal;

/**
 * 多表动态条件查询入参（photo_work JOIN photographer）。
 * 全部字段可空，XML 中用 &lt;where&gt; + &lt;if&gt; 拼装，条件为空即查全部。
 */
public class WorkQueryDTO {

    /** 作品风格精确匹配 */
    private String style;

    /** 拍摄地模糊匹配 */
    private String locationKeyword;

    /** 摄影师姓名模糊匹配 */
    private String photographerName;

    /** 所属工作室精确匹配 */
    private String studio;

    /** 报价下限 */
    private BigDecimal minPrice;

    /** 报价上限 */
    private BigDecimal maxPrice;

    /** 作品状态：1 已上架 0 已下架 */
    private Integer workStatus;

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getLocationKeyword() {
        return locationKeyword;
    }

    public void setLocationKeyword(String locationKeyword) {
        this.locationKeyword = locationKeyword;
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

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Integer getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(Integer workStatus) {
        this.workStatus = workStatus;
    }
}
