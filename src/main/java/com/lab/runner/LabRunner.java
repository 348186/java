package com.lab.runner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.dto.WorkQueryDTO;
import com.lab.entity.Photographer;
import com.lab.mapper.PhotoWorkMapper;
import com.lab.mapper.PhotographerMapper;
import com.lab.util.MybatisSessionUtil;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * 实验一 · 步骤 2.4 第一个 MyBatis 程序。
 * 直接以 main 方法运行，观察控制台 SQL 日志与结果映射。
 */
public class LabRunner {

    public static void main(String[] args) {
        System.out.println("========== 1. 原生 MyBatis：查询全部摄影师 ==========");
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper mapper = session.getMapper(PhotographerMapper.class);
            List<Photographer> all = mapper.findAll();
            all.forEach(System.out::println);
            System.out.println("摄影师总数 = " + all.size());
        }

        System.out.println("========== 2. 动态 SQL：多条件组合查询 ==========");
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper mapper = session.getMapper(PhotographerMapper.class);
            Photographer condition = new Photographer();
            condition.setStudio("人像工作室");
            condition.setGender("女");
            mapper.queryByExample(condition).forEach(System.out::println);
        }

        System.out.println("========== 3. MyBatis-Plus：条件构造器 + 分页 ==========");
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper mapper = session.getMapper(PhotographerMapper.class);
            LambdaQueryWrapper<Photographer> wrapper = new LambdaQueryWrapper<Photographer>()
                    .ge(Photographer::getDayRate, new java.math.BigDecimal("8000"))
                    .orderByDesc(Photographer::getDayRate);
            Page<Photographer> page = mapper.selectPage(new Page<>(1, 3), wrapper);
            System.out.println("总记录数 = " + page.getTotal());
            System.out.println("总页数   = " + page.getPages());
            page.getRecords().forEach(System.out::println);
        }

        System.out.println("========== 4. 多表查询：作品 JOIN 摄影师 ==========");
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotoWorkMapper workMapper = session.getMapper(PhotoWorkMapper.class);

            System.out.println("【4.1 联表查询作品详情】");
            workMapper.findWorkDetails().stream().limit(5).forEach(System.out::println);

            System.out.println("【4.2 动态条件联表：人像风格 + 报价 1000~2000】");
            WorkQueryDTO query = new WorkQueryDTO();
            query.setStyle("人像");
            query.setMinPrice(new java.math.BigDecimal("1000"));
            query.setMaxPrice(new java.math.BigDecimal("2000"));
            workMapper.findWorkDetailsByCondition(query).forEach(System.out::println);

            System.out.println("【4.3 按工作室统计：作品数 / 均价 / 最高价 / 最低价】");
            workMapper.statByStudio().forEach(System.out::println);
        }

        System.out.println("========== 5. 多表查询：摄影师 + 作品（一对多 collection） ==========");
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            session.getMapper(PhotographerMapper.class).findWithWorks().stream()
                    .limit(3)
                    .forEach(p -> {
                        System.out.println(p.getPhotographerName() + "（" + p.getStudio()
                                + "）共 " + p.getWorks().size() + " 件作品");
                        p.getWorks().forEach(w -> System.out.println("    - " + w));
                    });
        }
    }
}
