package com.lab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.entity.Photographer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 摄影师持久层接口。
 * 继承 BaseMapper 后即拥有 MP 通用 CRUD；下列方法为手写 XML 动态 SQL，
 * 与 BaseMapper 共存（方法命名刻意避开 BaseMapper 已有签名，防止重载歧义）。
 */
public interface PhotographerMapper extends BaseMapper<Photographer> {

    /** 查询全部摄影师 */
    List<Photographer> findAll();

    /** 按主键查询 */
    Photographer findByPrimaryKey(Integer photographerId);

    /** 多条件组合查询（if + where） */
    List<Photographer> queryByExample(Photographer condition);

    /** 分支查询：优先姓名精确 → 其次工作室 → 否则全表（choose / when / otherwise） */
    List<Photographer> queryByPriority(Photographer condition);

    /** 新增摄影师，回填自增主键 */
    int addPhotographer(Photographer photographer);

    /** 批量新增 */
    int addInBatch(@Param("photographerList") List<Photographer> photographerList);

    /** 全量更新 */
    int modifyPhotographer(Photographer photographer);

    /** 选择性更新（set + if） */
    int modifySelective(Photographer photographer);

    /** 按主键删除 */
    int removePhotographer(Integer photographerId);

    /** 批量删除 */
    int removeInBatch(@Param("ids") List<Integer> ids);

    /**
     * 一对多多表查询：摄影师 LEFT JOIN 作品，
     * 结果用 &lt;collection&gt; 嵌套进 {@code Photographer.works}（无作品的摄影师也会有记录，works 为空集合）。
     */
    List<Photographer> findWithWorks();
}
