package com.example;

import com.example.entity.User;
import com.example.mapper.UserMapper;
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

class MybatisTest {

    private SqlSession sqlSession;
    private UserMapper userMapper;

    @BeforeEach
    void setUp() throws IOException {
        InputStream is = Resources.getResourceAsStream("chapper01/mabatis.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
        sqlSession = sqlSessionFactory.openSession();
        userMapper = sqlSession.getMapper(UserMapper.class);

        User base = userMapper.testfind("testuser1");
        if (base == null) {
            base = new User(null, "testuser1", "password123", "testuser1@example.com");
            userMapper.addUser(base);
        }
        sqlSession.commit();
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
    void testFindAllByxml() {
        List<User> users = userMapper.findAllByxml();
        assertNotNull(users);
        System.out.println("=== findAllByxml ===");
        users.forEach(System.out::println);
    }

    @Test
    void testSelectAll() {
        List<User> users = userMapper.selectAll();
        assertNotNull(users);
        System.out.println("=== selectAll ===");
        users.forEach(System.out::println);
    }

    @Test
    void testFindById() {
        User base = userMapper.testfind("testuser1");
        assertNotNull(base);
        User user = userMapper.findById(base.getId());
        assertNotNull(user);
        System.out.println("=== findById(" + base.getId() + ") ===");
        System.out.println(user);
    }

    @Test
    void testSelectById() {
        User base = userMapper.testfind("testuser1");
        assertNotNull(base);
        User user = userMapper.selectById(base.getId());
        assertNotNull(user);
        System.out.println("=== selectById(" + base.getId() + ") ===");
        System.out.println(user);
    }

    @Test
    void testTestfind() {
        User user = userMapper.testfind("testuser1");
        assertNotNull(user);
        System.out.println("=== testfind(\"testuser1\") ===");
        System.out.println(user);
    }

    @Test
    void testAddUser() {
        String suffix = uniqueSuffix();
        User user = new User(null, "test_user_" + suffix, "123456", "test_" + suffix + "@example.com");
        int rows = userMapper.addUser(user);
        sqlSession.commit();
        assertTrue(rows > 0);
        assertNotNull(user.getId());
        System.out.println("=== addUser ===");
        System.out.println("影响行数: " + rows + ", 自增主键: " + user.getId());
    }

    @Test
    void testInsert() {
        String suffix = uniqueSuffix();
        User user = new User(null, "insert_user_" + suffix, "password", "insert_" + suffix + "@example.com");
        int rows = userMapper.insert(user);
        sqlSession.commit();
        assertTrue(rows > 0);
        assertNotNull(user.getId());
        System.out.println("=== insert ===");
        System.out.println("影响行数: " + rows + ", 自增主键: " + user.getId());
    }

    @Test
    void testUpdateUser() {
        String suffix = uniqueSuffix();
        User user = new User(null, "upd_prep_" + suffix, "pwd", "updprep_" + suffix + "@example.com");
        userMapper.addUser(user);
        sqlSession.commit();
        user.setEmail("updated_" + suffix + "@example.com");
        int rows = userMapper.updateUser(user);
        sqlSession.commit();
        assertTrue(rows > 0);
        System.out.println("=== updateUser ===");
        System.out.println("影响行数: " + rows);
    }

    @Test
    void testUpdate() {
        String suffix = uniqueSuffix();
        User user = new User(null, "upd2_prep_" + suffix, "pwd", "upd2prep_" + suffix + "@example.com");
        userMapper.addUser(user);
        sqlSession.commit();
        user.setEmail("update_alias_" + suffix + "@example.com");
        int rows = userMapper.update(user);
        sqlSession.commit();
        assertTrue(rows > 0);
        System.out.println("=== update ===");
        System.out.println("影响行数: " + rows);
    }

    @Test
    void testDeleteUser() {
        String suffix = uniqueSuffix();
        User user = new User(null, "to_delete_" + suffix, "pwd", "del_" + suffix + "@example.com");
        userMapper.addUser(user);
        sqlSession.commit();
        int rows = userMapper.deleteUser(user.getId());
        sqlSession.commit();
        assertTrue(rows > 0);
        System.out.println("=== deleteUser ===");
        System.out.println("影响行数: " + rows);
    }

    @Test
    void testDeleteById() {
        String suffix = uniqueSuffix();
        User user = new User(null, "to_del_" + suffix, "pwd", "del2_" + suffix + "@example.com");
        userMapper.insert(user);
        sqlSession.commit();
        int rows = userMapper.deleteById(user.getId());
        sqlSession.commit();
        assertTrue(rows > 0);
        System.out.println("=== deleteById ===");
        System.out.println("影响行数: " + rows);
    }
}