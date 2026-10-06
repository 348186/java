package com.example;

import com.example.entity.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UseMapperTest {

    private SqlSessionFactory sqlSessionFactory;
    private SqlSession sqlSession;

    @BeforeEach
    public void init() throws Exception {
        InputStream is = Resources.getResourceAsStream("chapper01/mabatis.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    }

    @AfterEach
    public void destroy() {
        if (sqlSession != null) {
            sqlSession.close();
        }
    }

    private String ns() {
        return "com.example.mapper.UserMapper";
    }

    private String uniqueSuffix() {
        return System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    @Test
    public void testFindAllByXml() {
        System.out.println("========== 测试查询所有用户 (XML方式) ==========");
        sqlSession = sqlSessionFactory.openSession();
        List<User> users = sqlSession.selectList(ns() + ".findAllByxml");
        for (User user : users) {
            System.out.println(user);
        }
    }

    @Test
    public void testFindByIdByXml() {
        System.out.println("========== 测试根据ID查询用户 (XML方式) ==========");
        sqlSession = sqlSessionFactory.openSession();
        User user = sqlSession.selectOne(ns() + ".findById", 1);
        System.out.println(user);
    }

    @Test
    public void testFindByNameAndPass() {
        System.out.println("========== 测试多条件查询 (XML方式) ==========");
        sqlSession = sqlSessionFactory.openSession();
        User user = new User();
        user.setUsername("testuser1");
        user.setPassword("password123");

        List<User> users = sqlSession.selectList(ns() + ".findByNameAndPass", user);
        System.out.println(users);
    }

    @Test
    public void testFindCountByXml() {
        System.out.println("========== 测试查询聚合函数 (XML方式) ==========");
        sqlSession = sqlSessionFactory.openSession();
        int count = sqlSession.selectOne(ns() + ".findCount");
        System.out.println(count);
    }

    @Test
    public void testFindByUsernameByXml() {
        System.out.println("========== 测试根据姓名查询用户 (XML方式) ==========");
        sqlSession = sqlSessionFactory.openSession();
        List<User> users = sqlSession.selectList(ns() + ".findByUsernameXML", "testuser1");
        System.out.println(users);
    }

    @Test
    public void testInsertByXml() {
        System.out.println("========== 测试添加用户 (XML方式) ==========");
        sqlSession = sqlSessionFactory.openSession();
        User user = new User();
        user.setUsername("Jacker_" + uniqueSuffix());
        user.setPassword("12334");
        user.setEmail("testxxx_" + uniqueSuffix() + "@qq.com");
        int rows = sqlSession.insert(ns() + ".addUser", user);
        System.out.println("影响行数：" + rows + ", 自增主键：" + user.getId());
        sqlSession.commit();
    }

    @Test
    public void testFindByMap() {
        System.out.println("========== 测试Map多条件查询 ==========");
        sqlSession = sqlSessionFactory.openSession();
        Map<String, Object> map = new HashMap<>();
        map.put("abc", "password123");
        map.put("username", "testuser1");
        List<User> users = sqlSession.selectList(ns() + ".findByMap", map);
        System.out.println(users);
    }

    @Test
    public void testUpdateUserByXml() {
        System.out.println("========== 测试更新用户 (XML方式) ==========");
        sqlSession = sqlSessionFactory.openSession();
        User user = new User();
        user.setUsername("upd_src_" + uniqueSuffix());
        user.setPassword("12334");
        user.setEmail("src_" + uniqueSuffix() + "@qq.com");
        sqlSession.insert(ns() + ".addUser", user);
        sqlSession.commit();

        user.setUsername("upd_done_" + uniqueSuffix());
        user.setEmail("upd_email_" + uniqueSuffix() + "@qq.com");
        user.setPassword("4321");
        int rows = sqlSession.update(ns() + ".updateUser", user);
        System.out.println("影响行数：" + rows);
        sqlSession.commit();
    }
}