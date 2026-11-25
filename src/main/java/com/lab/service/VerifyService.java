package com.lab.service;

import com.lab.entity.PhotoWorkPlus;
import com.lab.mapper.PhotoWorkPlusMapper;
import org.springframework.stereotype.Service;

/**
 * 步骤 3.4 临时验证 Service：调用 MP 的 selectById 检查数据库连通与事务装配。
 * 整合阶段先保证"连得上、映射对、能注入"，再进入业务接口开发。
 */
@Service
public class VerifyService {

    private final PhotoWorkPlusMapper photoWorkPlusMapper;

    public VerifyService(PhotoWorkPlusMapper photoWorkPlusMapper) {
        this.photoWorkPlusMapper = photoWorkPlusMapper;
    }

    public String verifyDb() {
        try {
            PhotoWorkPlus work = photoWorkPlusMapper.selectById(1);
            return "DB OK: " + (work == null
                    ? "no row with work_id=1"
                    : work.getWorkTitle() + " / " + work.getStyle() + " / " + work.getPrice());
        } catch (Exception e) {
            return "DB FAIL: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }
}
