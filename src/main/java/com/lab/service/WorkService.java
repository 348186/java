package com.lab.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lab.dto.WorkQueryDTO;
import com.lab.entity.PhotoWorkPlus;

/**
 * 作品业务接口：继承 IService 获得基础 CRUD，并扩展分页查询与事务验证方法。
 */
public interface WorkService extends IService<PhotoWorkPlus> {

    /** 分页 + 多条件查询，按拍摄日期倒序 */
    Page<PhotoWorkPlus> pageQuery(int page, int size, WorkQueryDTO query);

    /** 以下 5 个方法用于声明式事务的五种场景验证 */
    void saveWithoutTx(PhotoWorkPlus work);

    void saveWithTx(PhotoWorkPlus work);

    void saveWithTxCatchException(PhotoWorkPlus work);

    void saveWithTxSelfInvoke(PhotoWorkPlus work);

    void saveWithTxPrivate(PhotoWorkPlus work);

    /** 按标题统计/清理事务测试数据 */
    long countByTitle(String title);

    int deleteByTitle(String title);
}
