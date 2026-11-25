package com.lab.mapper;

import com.lab.entity.PhotoWork;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 摄影作品 Mapper：手写 XML SQL 版本。
 * photo_work 通过 photographer_id 关联 photographer 表。
 */
public interface PhotoWorkMapper {

    /** 查询全部作品（联表带出摄影师姓名） */
    List<PhotoWork> selectAll();

    /** 按主键查询（联表带出摄影师姓名） */
    PhotoWork selectById(@Param("workId") Integer workId);

    /** 新增作品，主键回填到 workId */
    int insert(PhotoWork work);

    /** 按主键更新全部字段 */
    int update(PhotoWork work);

    /** 按主键删除 */
    int deleteById(@Param("workId") Integer workId);

    /** 多条件模糊查询（动态 SQL：if + where） */
    List<PhotoWork> selectByCondition(PhotoWork condition);

    /** 动态更新（set 标签，只更新非空字段） */
    int updateSelective(PhotoWork work);

    /** 批量删除（foreach） */
    int deleteByIds(@Param("ids") Integer[] ids);

    /** 批量插入（foreach） */
    int insertBatch(@Param("works") List<PhotoWork> works);

    /** 分支选择：按风格/摄影师编号/在展作品（choose/when/otherwise） */
    List<PhotoWork> selectByChoice(@Param("style") String style,
                                   @Param("photographerId") Integer photographerId);

    /** 按风格分组统计数量与平均定价（结果用 Map 接收） */
    List<Map<String, Object>> groupByStyle();
}
