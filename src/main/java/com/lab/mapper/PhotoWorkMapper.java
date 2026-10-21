package com.lab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.dto.WorkQueryDTO;
import com.lab.entity.PhotoWork;
import com.lab.vo.StudioStatVO;
import com.lab.vo.WorkDetailVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 摄影作品持久层接口（多表查询集中在这里）。
 *
 * <p>覆盖四类典型多表查询：
 * <ol>
 *   <li>普通联表：{@link #findWorkDetails()} —— photo_work JOIN photographer，结果封装成 VO</li>
 *   <li>动态条件联表：{@link #findWorkDetailsByCondition(WorkQueryDTO)} —— &lt;where&gt; + &lt;if&gt; 拼装</li>
 *   <li>按从表条件反查主表维度：{@link #findWorksByStudio(String)}</li>
 *   <li>分组聚合：{@link #statByStudio()} —— GROUP BY studio 统计作品数与价格</li>
 * </ol>
 */
public interface PhotoWorkMapper extends BaseMapper<PhotoWork> {

    /** 按摄影师编号查询其作品（单表，供对照） */
    List<PhotoWork> findByPhotographerId(@Param("photographerId") Integer photographerId);

    /** 联表查询全部作品详情（作品 + 摄影师） */
    List<WorkDetailVO> findWorkDetails();

    /** 联表 + 动态条件查询 */
    List<WorkDetailVO> findWorkDetailsByCondition(WorkQueryDTO query);

    /** 按工作室查询作品详情 */
    List<WorkDetailVO> findWorksByStudio(@Param("studio") String studio);

    /** 按工作室分组统计：作品数 / 均价 / 最高价 / 最低价 */
    List<StudioStatVO> statByStudio();
}
