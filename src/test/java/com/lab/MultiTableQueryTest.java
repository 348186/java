package com.lab;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.dto.WorkQueryDTO;
import com.lab.entity.PhotoWork;
import com.lab.entity.Photographer;
import com.lab.mapper.PhotoWorkMapper;
import com.lab.mapper.PhotographerMapper;
import com.lab.util.MybatisSessionUtil;
import com.lab.vo.StudioStatVO;
import com.lab.vo.WorkDetailVO;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 多表查询单元测试：photo_work（作品）JOIN photographer（摄影师）。
 *
 * <p>覆盖四类写法：普通联表、动态条件联表、按主表维度反查、GROUP BY 分组聚合，
 * 以及一对多的 &lt;collection&gt; 嵌套结果。
 */
class MultiTableQueryTest {

    @Test
    @DisplayName("多表-1：INNER JOIN 查询作品详情（作品 + 摄影师 + 工作室）")
    void testFindWorkDetails() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotoWorkMapper workMapper = session.getMapper(PhotoWorkMapper.class);
            List<WorkDetailVO> details = workMapper.findWorkDetails();
            details.forEach(System.out::println);

            // 联表不会丢数据：条数应与作品总行数一致
            assertEquals(workMapper.selectCount(null), (long) details.size());
            // 每条记录都必须带上摄影师侧的信息，否则说明 JOIN 或结果映射有问题
            details.forEach(d -> {
                assertNotNull(d.getPhotographerName(), "联查结果应带回摄影师姓名");
                assertNotNull(d.getStudio(), "联查结果应带回所属工作室");
            });
            // 按价格倒序
            for (int i = 1; i < details.size(); i++) {
                assertTrue(details.get(i - 1).getPrice().compareTo(details.get(i).getPrice()) >= 0);
            }
        }
    }

    @Test
    @DisplayName("多表-2：动态条件联表查询（风格 + 拍摄地 + 摄影师 + 价格区间）")
    void testFindWorkDetailsByCondition() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotoWorkMapper workMapper = session.getMapper(PhotoWorkMapper.class);

            // 无条件 = 全表联查
            assertEquals(workMapper.selectCount(null),
                    (long) workMapper.findWorkDetailsByCondition(new WorkQueryDTO()).size());

            // 单条件：风格
            WorkQueryDTO byStyle = new WorkQueryDTO();
            byStyle.setStyle("风光");
            List<WorkDetailVO> scenery = workMapper.findWorkDetailsByCondition(byStyle);
            assertFalse(scenery.isEmpty());
            scenery.forEach(d -> assertEquals("风光", d.getStyle()));
            assertEquals(workMapper.selectCount(
                            new LambdaQueryWrapper<PhotoWork>().eq(PhotoWork::getStyle, "风光")),
                    (long) scenery.size());

            // 跨表条件：摄影师姓名模糊 + 价格区间
            WorkQueryDTO mixed = new WorkQueryDTO();
            mixed.setPhotographerName("张");
            mixed.setMinPrice(new BigDecimal("1000"));
            mixed.setMaxPrice(new BigDecimal("2000"));
            List<WorkDetailVO> mixedList = workMapper.findWorkDetailsByCondition(mixed);
            assertFalse(mixedList.isEmpty());
            mixedList.forEach(d -> {
                assertTrue(d.getPhotographerName().contains("张"));
                assertTrue(d.getPrice().compareTo(new BigDecimal("1000")) >= 0);
                assertTrue(d.getPrice().compareTo(new BigDecimal("2000")) <= 0);
            });

            // 主表（摄影师）条件：工作室
            WorkQueryDTO byStudio = new WorkQueryDTO();
            byStudio.setStudio("人像工作室");
            List<WorkDetailVO> studioWorks = workMapper.findWorkDetailsByCondition(byStudio);
            assertFalse(studioWorks.isEmpty());
            studioWorks.forEach(d -> assertEquals("人像工作室", d.getStudio()));

            // 拍摄地模糊
            WorkQueryDTO byLocation = new WorkQueryDTO();
            byLocation.setLocationKeyword("上海");
            List<WorkDetailVO> shanghai = workMapper.findWorkDetailsByCondition(byLocation);
            assertFalse(shanghai.isEmpty());
            shanghai.forEach(d -> assertTrue(d.getLocation().contains("上海")));
        }
    }

    @Test
    @DisplayName("多表-3：按工作室反查作品（主表维度过滤从表）")
    void testFindWorksByStudio() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotoWorkMapper workMapper = session.getMapper(PhotoWorkMapper.class);
            List<WorkDetailVO> works = workMapper.findWorksByStudio("旅拍小队");
            works.forEach(System.out::println);

            assertFalse(works.isEmpty());
            works.forEach(d -> assertEquals("旅拍小队", d.getStudio()));

            // 与「先查摄影师再查作品」的结果交叉校验
            long expected = 0;
            try (SqlSession s2 = MybatisSessionUtil.openSession()) {
                Photographer condition = new Photographer();
                condition.setStudio("旅拍小队");
                List<Photographer> photographers =
                        s2.getMapper(PhotographerMapper.class).queryByExample(condition);
                for (Photographer p : photographers) {
                    expected += workMapper.selectCount(new LambdaQueryWrapper<PhotoWork>()
                            .eq(PhotoWork::getPhotographerId, p.getPhotographerId()));
                }
            }
            assertEquals(expected, (long) works.size());
        }
    }

    @Test
    @DisplayName("多表-4：GROUP BY 分组聚合（各工作室作品数 / 均价 / 最高价 / 最低价）")
    void testStatByStudio() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotoWorkMapper workMapper = session.getMapper(PhotoWorkMapper.class);
            List<StudioStatVO> stats = workMapper.statByStudio();
            stats.forEach(System.out::println);

            assertFalse(stats.isEmpty());
            long total = 0;
            for (StudioStatVO s : stats) {
                assertNotNull(s.getStudio());
                assertTrue(s.getWorkCount() > 0);
                assertTrue(s.getAvgPrice().compareTo(s.getMinPrice()) >= 0);
                assertTrue(s.getAvgPrice().compareTo(s.getMaxPrice()) <= 0);
                total += s.getWorkCount();
            }
            // 分组统计的作品总数 = 作品表总行数（INNER JOIN 保证每件作品都有归属摄影师）
            assertEquals(workMapper.selectCount(null), total);
        }
    }

    @Test
    @DisplayName("多表-5：一对多 collection 嵌套（摄影师 + 作品列表）")
    void testFindWithWorks() {
        try (SqlSession session = MybatisSessionUtil.openSession()) {
            PhotographerMapper photographerMapper = session.getMapper(PhotographerMapper.class);
            PhotoWorkMapper workMapper = session.getMapper(PhotoWorkMapper.class);

            List<Photographer> list = photographerMapper.findWithWorks();
            // LEFT JOIN + collection 会把重复的主表行合并，人数应与摄影师表一致
            assertEquals(photographerMapper.selectCount(null), (long) list.size());

            Photographer first = list.get(0);
            System.out.println(first.getPhotographerName() + " 的作品：");
            first.getWorks().forEach(w -> System.out.println("   " + w));

            // 嵌套的作品数量应与单表按 photographerId 查询一致
            assertEquals(workMapper.selectCount(new LambdaQueryWrapper<PhotoWork>()
                            .eq(PhotoWork::getPhotographerId, first.getPhotographerId())),
                    (long) first.getWorks().size());

            // 每个摄影师下的作品外键都应指向自己
            list.forEach(p -> p.getWorks().forEach(
                    w -> assertEquals(p.getPhotographerId(), w.getPhotographerId())));

            // 全部作品都被挂载到某个摄影师下
            long mounted = list.stream().mapToLong(p -> p.getWorks().size()).sum();
            assertEquals(workMapper.selectCount(null), mounted);
        }
    }
}
