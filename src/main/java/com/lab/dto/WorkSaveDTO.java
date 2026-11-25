package com.lab.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 作品新增/修改请求体：只声明接口关心的字段，不把校验注解加到数据库实体上。
 */
public class WorkSaveDTO {

    @NotNull(message = "摄影师编号不能为空")
    @Positive(message = "摄影师编号必须为正数")
    private Integer photographerId;

    @NotBlank(message = "作品标题不能为空")
    @Size(max = 80, message = "作品标题不能超过80个字符")
    private String workTitle;

    @Size(max = 100, message = "拍摄地点不能超过100个字符")
    private String location;

    @NotNull(message = "拍摄日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date shootDate;

    @NotBlank(message = "作品风格不能为空")
    @Size(max = 40, message = "作品风格不能超过40个字符")
    private String style;

    @NotNull(message = "作品定价不能为空")
    @DecimalMin(value = "0", message = "作品定价不能为负数")
    @DecimalMax(value = "999999.99", message = "作品定价超出上限")
    private BigDecimal price;

    @NotNull(message = "展示状态不能为空")
    @Min(value = 0, message = "展示状态只能是0或1")
    @Max(value = 1, message = "展示状态只能是0或1")
    private Integer status;

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
}
