package com.lab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.dto.WorkQueryDTO;
import com.lab.entity.PhotoWorkPlus;
import com.lab.mapper.PhotoWorkPlusMapper;
import com.lab.service.WorkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 作品业务实现：查询条件用 Lambda 引用字段，编译期即可发现字段名错误。
 */
@Service
public class WorkServiceImpl extends ServiceImpl<PhotoWorkPlusMapper, PhotoWorkPlus>
        implements WorkService {

    @Override
    public Page<PhotoWorkPlus> pageQuery(int page, int size, WorkQueryDTO query) {
        WorkQueryDTO q = query == null ? new WorkQueryDTO() : query;
        int pageNo = page < 1 ? 1 : page;
        int pageSize = size < 1 ? 5 : size;
        LambdaQueryWrapper<PhotoWorkPlus> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(q.getWorkTitle()),
                        PhotoWorkPlus::getWorkTitle, q.getWorkTitle())
                .eq(StringUtils.hasText(q.getStyle()), PhotoWorkPlus::getStyle, q.getStyle())
                .eq(q.getPhotographerId() != null,
                        PhotoWorkPlus::getPhotographerId, q.getPhotographerId())
                .eq(q.getStatus() != null, PhotoWorkPlus::getStatus, q.getStatus())
                .ge(q.getMinPrice() != null, PhotoWorkPlus::getPrice, q.getMinPrice())
                .le(q.getMaxPrice() != null, PhotoWorkPlus::getPrice, q.getMaxPrice())
                .orderByDesc(PhotoWorkPlus::getShootDate);
        return baseMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    }

    /** 场景1：没有事务注解，异常抛出后数据保留 */
    @Override
    public void saveWithoutTx(PhotoWorkPlus work) {
        baseMapper.insert(work);
        throw new RuntimeException("无事务：插入后故意抛出异常");
    }

    /** 场景2：代理对象公开方法上的事务正常回滚 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithTx(PhotoWorkPlus work) {
        baseMapper.insert(work);
        throw new RuntimeException("有事务：插入后故意抛出异常");
    }

    /** 场景3：异常被 catch 吞掉，事务拦截器看不到异常，提交数据 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithTxCatchException(PhotoWorkPlus work) {
        try {
            baseMapper.insert(work);
            throw new RuntimeException("事务失效：异常被 try-catch 吞掉");
        } catch (RuntimeException e) {
            System.out.println("[TX-CATCH] " + e.getMessage());
        }
    }

    /** 场景4：同类内部调用绕过代理，事务注解不生效 */
    @Override
    public void saveWithTxSelfInvoke(PhotoWorkPlus work) {
        this.saveWithTxInner(work);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveWithTxInner(PhotoWorkPlus work) {
        baseMapper.insert(work);
        throw new RuntimeException("事务失效：同类自调用绕过代理");
    }

    /** 场景5：private 方法无法被 AOP 代理，事务注解不生效 */
    @Override
    public void saveWithTxPrivate(PhotoWorkPlus work) {
        saveWithTxPrivateInner(work);
    }

    @Transactional(rollbackFor = Exception.class)
    private void saveWithTxPrivateInner(PhotoWorkPlus work) {
        baseMapper.insert(work);
        throw new RuntimeException("事务失效：private 方法不被代理");
    }

    @Override
    public long countByTitle(String title) {
        LambdaQueryWrapper<PhotoWorkPlus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoWorkPlus::getWorkTitle, title);
        return baseMapper.selectCount(wrapper);
    }

    @Override
    public int deleteByTitle(String title) {
        LambdaQueryWrapper<PhotoWorkPlus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoWorkPlus::getWorkTitle, title);
        return baseMapper.delete(wrapper);
    }
}
