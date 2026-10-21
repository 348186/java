package com.lab.cache.annotation;

import com.lab.cache.TimedLruCache;
import com.lab.entity.Photographer;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Property;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * 缓存实验专用 Mapper（<strong>注解方式</strong>）。
 *
 * <p>{@code @CacheNamespace} 与 XML 的 {@code <cache/>} 等价，二者<strong>不能用于同一个 namespace</strong>，
 * 否则启动时会报 "Caches collection already contains value for ..."。
 *
 * <p>小知识点：注解 {@code size} / {@code flushInterval} 这两个属性只对内置的
 * {@code PerpetualCache} 生效；自定义实现要用 properties 传参，MyBatis 会按 setter 类型做转换后注入。
 */
@CacheNamespace(implementation = TimedLruCache.class,
        properties = {@Property(name = "size", value = "256"), @Property(name = "ttlSeconds", value = "30")})
public interface PhotographerCacheAnnotationMapper {

    @Select("SELECT photographer_id, photographer_name, gender, studio, specialty, day_rate, join_date, status "
            + "FROM photographer WHERE photographer_id = #{photographerId}")
    @Results(id = "photographerResult", value = {
            @Result(property = "photographerId", column = "photographer_id", id = true),
            @Result(property = "photographerName", column = "photographer_name"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "studio", column = "studio"),
            @Result(property = "specialty", column = "specialty"),
            @Result(property = "dayRate", column = "day_rate"),
            @Result(property = "joinDate", column = "join_date"),
            @Result(property = "status", column = "status")
    })
    Photographer findByPrimaryKey(@Param("photographerId") Integer photographerId);

    /** 等价于 XML 的 useCache="false"：结果不进二级缓存 */
    @Select("SELECT photographer_id, photographer_name, gender, studio, specialty, day_rate, join_date, status "
            + "FROM photographer ORDER BY photographer_id")
    @ResultMap("photographerResult")
    @Options(useCache = false)
    List<Photographer> findAll();

    @Update("UPDATE photographer SET day_rate = #{dayRate} WHERE photographer_id = #{photographerId}")
    int renewDayRate(@Param("photographerId") Integer photographerId, @Param("dayRate") BigDecimal dayRate);
}
