package com.lab.vo;

import java.math.BigDecimal;

/**
 * 工作室维度统计视图对象：GROUP BY studio 的多表聚合结果。
 */
public class StudioStatVO {

    private String studio;
    private Long workCount;
    private BigDecimal avgPrice;
    private BigDecimal maxPrice;
    private BigDecimal minPrice;

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public Long getWorkCount() {
        return workCount;
    }

    public void setWorkCount(Long workCount) {
        this.workCount = workCount;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    @Override
    public String toString() {
        return "StudioStatVO{studio='" + studio + '\''
                + ", workCount=" + workCount
                + ", avgPrice=" + avgPrice
                + ", maxPrice=" + maxPrice
                + ", minPrice=" + minPrice
                + '}';
    }
}
