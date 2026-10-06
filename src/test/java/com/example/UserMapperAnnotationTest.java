package com.example;

import com.example.entity.User;
import com.example.mapper.UserMapperAnnotation;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperAnnotationTest {

    private SqlSession sqlSession;
    private UserMapperAnnotation userMapperAnnotation;

    @BeforeEach
    void setUp() throws IOException {
        InputStream is = Resources.getResourceAsStream("chapper01/mabatis.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
        sqlSession = sqlSessionFactory.openSession();
        userMapperAnnotation = sqlSession.getMapper(UserMapperAnnotation.class);
    }

    @AfterEach
    void tearDown() {
        if (sqlSession != null) {
            sqlSession.close();
        }
    }

    private String uniqueSuffix() {
        return System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    @Test
    void testFindById() {
        System.out.println("========== 测试根据ID查询用户 (注解方式) ==========");
        User user = userMapperAnnotation.findById(1);
        assertNotNull(user);
        System.out.println(user);
    }

    @Test
    void testFindAll() {
        System.out.println("========== 测试查询所有用户 (注解方式) ==========");
        List<User> users = userMapperAnnotation.findAll();
        assertNotNull(users);
        System.out.println("共查询到 " + users.size() + " 条记录");
        users.forEach(System.out::println);
    }

    @Test
    void testInsert() {
        System.out.println("========== 测试添加用户 (注解方式) ==========");
        String suffix = uniqueSuffix();
        User user = new User(null, "annot_user_" + suffix, "123456", "annot_" + suffix + "@example.com");
        int rows = userMapperAnnotation.insert(user);
        sqlSession.commit();
        assertTrue(rows > 0);
        System.out.println("影响行数: " + rows);
    }

    @Test
    void testUpdate() {
        System.out.println("========== 测试更新用户 (注解方式) ==========");
        String suffix = uniqueSuffix();
        User user = new User(null, "annot_upd_" + suffix, "pwd", "annotupd_" + suffix + "@example.com");
        userMapperAnnotation.insert(user);
        sqlSession.commit();

        user.setUsername("annot_upd_done_" + suffix);
        user.setPassword("654321");
        user.setEmail("annot_done_" + suffix + "@example.com");
        int rows = userMapperAnnotation.update(user);
        sqlSession.commit();
        assertTrue(rows > 0);
        System.out.println("影响行数: " + rows);
    }

    @Test
    void testDeleteById() {
        System.out.println("========== 测试删除用户 (注解方式) ==========");
        String suffix = uniqueSuffix();
        User user = new User(null, "annot_del_" + suffix, "pwd", "annotdel_" + suffix + "@example.com");
        userMapperAnnotation.insert(user);
        sqlSession.commit();

        int rows = userMapperAnnotation.deleteById(user.getId());
        sqlSession.commit();
        assertTrue(rows > 0);
        System.out.println("影响行数: " + rows);
    }
}
