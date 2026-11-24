package com.lab;

import com.lab.entity.PhotoWork;
import com.lab.mapper.PhotoWorkMapper;
import com.lab.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MyBatis 手写 XML 版本控制台演示入口。
 * 覆盖：基础 CRUD、动态 SQL、批量操作、分组统计。
 */
public class Main {
    public static void main(String[] args) throws Exception {
        SqlSession session = MyBatisUtil.openSession();
        try {
            PhotoWorkMapper mapper = session.getMapper(PhotoWorkMapper.class);

            System.out.println("===== 1. 查询全部作品 =====");
            List<PhotoWork> all = mapper.selectAll();
            all.forEach(System.out::println);

            System.out.println("\n===== 2. 按主键查询 =====");
            System.out.println(mapper.selectById(1));

            System.out.println("\n===== 3. 新增作品（主键回填） =====");
            PhotoWork fresh = new PhotoWork("雨后植物园", "林越", "风光", "植物园外景",
                    new SimpleDateFormat("yyyy-MM-dd").parse("2025-06-25"),
                    new BigDecimal("3.0"), new BigDecimal("1280.00"), 1);
            int rows = mapper.insert(fresh);
            session.commit();
            System.out.println("影响行数=" + rows + ", 回填主键 workId=" + fresh.getWorkId());

            System.out.println("\n===== 4. 动态更新（只改价格） =====");
            PhotoWork patch = new PhotoWork();
            patch.setWorkId(fresh.getWorkId());
            patch.setPrice(new BigDecimal("1380.00"));
            System.out.println("影响行数=" + mapper.updateSelective(patch));
            session.commit();
            System.out.println(mapper.selectById(fresh.getWorkId()));

            System.out.println("\n===== 5. 多条件模糊查询 =====");
            PhotoWork condition = new PhotoWork();
            condition.setShootType("人像");
            condition.setStatus(1);
            mapper.selectByCondition(condition).forEach(System.out::println);

            System.out.println("\n===== 6. choose 分支查询（题材优先） =====");
            mapper.selectByChoice("街拍", "苏晴").forEach(w ->
                    System.out.println(w.getWorkTitle() + " / " + w.getShootType()));

            System.out.println("\n===== 7. 按题材分组统计 =====");
            List<Map<String, Object>> stats = mapper.groupByShootType();
            stats.forEach(m -> System.out.println(
                    m.get("shootType") + " -> 数量=" + m.get("workCount")
                            + ", 均价=" + m.get("avgPrice")));

            System.out.println("\n===== 8. 批量插入 + 批量删除 =====");
            PhotoWork w1 = new PhotoWork("夜色咖啡馆", "程野", "静物", "C影棚",
                    new SimpleDateFormat("yyyy-MM-dd").parse("2025-07-01"),
                    new BigDecimal("2.0"), new BigDecimal("880.00"), 1);
            PhotoWork w2 = new PhotoWork("山间小路", "林越", "风光", "西山外景",
                    new SimpleDateFormat("yyyy-MM-dd").parse("2025-07-03"),
                    new BigDecimal("4.0"), new BigDecimal("1180.00"), 1);
            mapper.insertBatch(Arrays.asList(w1, w2));
            session.commit();
            System.out.println("批量插入后总数=" + mapper.selectAll().size());
            mapper.deleteByIds(new Integer[]{w1.getWorkId(), w2.getWorkId()});
            session.commit();
            System.out.println("批量删除后总数=" + mapper.selectAll().size());

            System.out.println("\n===== 9. 全字段更新后删除演示作品 =====");
            PhotoWork target = mapper.selectById(fresh.getWorkId());
            target.setWorkTitle("雨后植物园（终稿）");
            System.out.println("全字段更新影响行数=" + mapper.update(target));
            session.commit();
            System.out.println("删除影响行数=" + mapper.deleteById(fresh.getWorkId()));
            session.commit();
        } finally {
            session.close();  // 会话用完必须关闭
        }
    }
}
