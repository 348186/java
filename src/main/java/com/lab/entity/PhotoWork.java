package com.lab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 摄影作品实体：与 photo_studio.photo_work 表一一对应。
 *
 * <p>photographerId 是外键，指向 {@link Photographer}；本表与摄影师表构成一对多关系，
 * 多表查询（JOIN / collection 嵌套）都基于这个外键展开。
 */
@TableName("photo_work")
public class PhotoWork implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 作品编号，数据库自增并回填 */
    @TableId(value = "work_id", type = IdType.AUTO)
    private Integer workId;

    /** 所属摄影师编号 */
    private Integer photographerId;

    /** 作品名称 */
    private String workTitle;

    /** 拍摄地 */
    private String location;

    /** 拍摄日期 */
    private LocalDate shootDate;

    /** 风格：人像 / 风光 / 婚礼 / 纪实 / 商业 / 旅拍 / 航拍 / 星空 / 建筑 / 儿童 / 美食 / 宠物 / 后期 */
    private String style;

    /** 报价 */
    private BigDecimal price;

    /** 状态：1 已上架，0 已下架 */
    private Integer status;

    public PhotoWork() {
    }

    public PhotoWork(Integer photographerId, String workTitle, String location,
                     LocalDate shootDate, String style, BigDecimal price, Integer status) {
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
                + '}';
    }
}
