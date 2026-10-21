package com.lab.cache;

import com.lab.entity.Photographer;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 缓存实验专用 Mapper（XML 方式），namespace = {@code com.lab.cache.PhotographerCacheMapper}。
 *
 * <p>独立 namespace 的意义：MyBatis 的二级缓存<strong>以 namespace 为单位</strong>隔离，
 * 单独开辟一个 namespace，既能演示缓存，又不会污染实验一使用的
 * {@code com.lab.mapper.PhotographerMapper}（否则那边的单元测试会读到缓存里的旧数据）。
 */
public interface PhotographerCacheMapper {

    /**
     * 按主键查询：XML 中显式写了 {@code useCache="true"}，结果会写入二级缓存。
     */
    Photographer findByPrimaryKey(@Param("photographerId") Integer photographerId);

    /**
     * 按工作室查询：用于验证「不同 SQL + 不同参数 = 不同 CacheKey，互不共享」。
     */
    List<Photographer> findByStudio(@Param("studio") String studio);

    /**
     * 全表查询：XML 中写了 {@code useCache="false"}，无论查多少次都直接走库。
     */
    List<Photographer> findAll();

    /**
     * 实时全表查询：XML 中同时写了 {@code useCache="false"} 与 {@code flushCache="true"}，
     * 每次执行前都会先清空本 namespace 的二级缓存。
     */
    List<Photographer> findAllFresh();

    /**
     * 修改日薪：insert/update/delete 默认 {@code flushCache="true"}，
     * 提交后一级缓存与二级缓存同时被清空。
     */
    int renewDayRate(@Param("photographerId") Integer photographerId, @Param("dayRate") BigDecimal dayRate);
}
