package com.lab;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.entity.PhotoWorkPlus;
import com.lab.mapper.PhotoWorkPlusMapper;
import com.lab.util.MybatisPlusUtil;
import org.apache.ibatis.session.SqlSession;

import java.math.BigDecimal;
import java.util.List;

/**
 * MyBatis-Plus 零 SQL 演示入口：通用 CRUD、条件构造器、分页。
 */
public class PlusMain {
    public static void main(String[] args) {
        SqlSession session = MybatisPlusUtil.openSession();
        try {
            PhotoWorkPlusMapper mapper = session.getMapper(PhotoWorkPlusMapper.class);

            System.out.println("===== 1. BaseMapper 查询全部 =====");
            mapper.selectList(null).forEach(System.out::println);

            System.out.println("\n===== 2. 条件构造器：风格=人像 且 价格>=1200 =====");
            QueryWrapper<PhotoWorkPlus> qw = new QueryWrapper<>();
            qw.eq("style", "人像").ge("price", 1200).orderByDesc("shoot_date");
            List<PhotoWorkPlus> list = mapper.selectList(qw);
            list.forEach(System.out::println);

            System.out.println("\n===== 3. 分页查询：第 1 页，每页 5 条 =====");
            IPage<PhotoWorkPlus> page = mapper.selectPage(new Page<>(1, 5), null);
            System.out.println("总记录数=" + page.getTotal() + ", 总页数=" + page.getPages()
                    + ", 当前页条数=" + page.getRecords().size());
            page.getRecords().forEach(System.out::println);

            System.out.println("\n===== 4. 新增 + 按主键修改 + 删除 =====");
            PhotoWorkPlus work = new PhotoWorkPlus(1, "MP测试作品", "上海 测试路",
                    new java.util.Date(), "风光", new BigDecimal("999.00"), 1);
            int inserted = mapper.insert(work);
            session.commit();
            System.out.println("新增行数=" + inserted + ", 回填主键=" + work.getWorkId());

            work.setPrice(new BigDecimal("1099.00"));
            System.out.println("更新行数=" + mapper.updateById(work));
            session.commit();

            System.out.println("删除行数=" + mapper.deleteById(work.getWorkId()));
            session.commit();
            System.out.println("删除后剩余=" + mapper.selectCount(null));
        } finally {
            session.close();
        }
    }
}
