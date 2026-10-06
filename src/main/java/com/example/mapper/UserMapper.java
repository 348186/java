package com.example.mapper;

import com.example.entity.User;

import java.util.List;

public interface UserMapper {

    List<User> findAllByxml();

    User testfind(String username);

    User findById(Integer id);

    int addUser(User user);

    int updateUser(User user);

    int deleteUser(Integer id);

    User selectById(Integer id);

    int insert(User user);

    int update(User user);

    int deleteById(Integer id);

    List<User> selectAll();
}