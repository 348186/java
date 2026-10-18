package com.lab.emp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.emp.common.BusinessException;
import com.lab.emp.dto.EmpQueryDTO;
import com.lab.entity.Emp;
import com.lab.mapper.EmpMapper;
import com.lab.emp.service.EmpService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 员工业务实现。
 * 继承 ServiceImpl&lt;EmpMapper, Emp&gt; 后无需再写单表 CRUD 实现。
 */
@Service
public class EmpServiceImpl extends ServiceImpl<EmpMapper, Emp> implements EmpService {

    @Override
    public IPage<Emp> searchByPage(long current, long size, EmpQueryDTO query) {
        // 防御式编程：分页参数非法时给出明确提示，而不是把异常抛到前端
        if (current < 1) {
            current = 1;
        }
        if (size < 1) {
            size = 5;
        }
        LambdaQueryWrapper<Emp> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.like(StringUtils.isNotBlank(query.getName()), Emp::getEmpName, query.getName())
                    .eq(StringUtils.isNotBlank(query.getGender()), Emp::getGender, query.getGender())
                    .eq(StringUtils.isNotBlank(query.getDept()), Emp::getDept, query.getDept())
                    .eq(query.getStatus() != null, Emp::getStatus, query.getStatus())
                    .ge(query.getMinSalary() != null, Emp::getSalary, query.getMinSalary());
        }
        wrapper.orderByDesc(Emp::getEmpId);
        return baseMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public List<Emp> findTopSalary(int limit) {
        if (limit < 1) {
            throw new BusinessException("limit 必须大于 0");
        }
        LambdaQueryWrapper<Emp> wrapper = new LambdaQueryWrapper<Emp>()
                .eq(Emp::getStatus, 1)
                .orderByDesc(Emp::getSalary)
                .last("LIMIT " + limit);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 任务 6.5：声明式事务验证。
     * 加上 @Transactional 后，方法内抛出的 RuntimeException 会触发回滚，数据库不留痕。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAndFail(Emp emp) {
        baseMapper.insert(emp);
        throw new BusinessException("故意抛出异常，用于验证事务回滚");
    }
}
