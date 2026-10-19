package com.lab.cache.annotation;

import com.lab.cache.TimedLruCache;
import com.lab.entity.Emp;
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
public interface EmpCacheAnnotationMapper {

    @Select("SELECT emp_id, emp_name, gender, dept, post, salary, hire_date, status "
            + "FROM emp WHERE emp_id = #{empId}")
    @Results(id = "empResult", value = {
            @Result(property = "empId", column = "emp_id", id = true),
            @Result(property = "empName", column = "emp_name"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "dept", column = "dept"),
            @Result(property = "post", column = "post"),
            @Result(property = "salary", column = "salary"),
            @Result(property = "hireDate", column = "hire_date"),
            @Result(property = "status", column = "status")
    })
    Emp findByPrimaryKey(@Param("empId") Integer empId);

    /** 等价于 XML 的 useCache="false"：结果不进二级缓存 */
    @Select("SELECT emp_id, emp_name, gender, dept, post, salary, hire_date, status FROM emp ORDER BY emp_id")
    @ResultMap("empResult")
    @Options(useCache = false)
    List<Emp> findAll();

    @Update("UPDATE emp SET salary = #{salary} WHERE emp_id = #{empId}")
    int renewSalary(@Param("empId") Integer empId, @Param("salary") BigDecimal salary);
}
