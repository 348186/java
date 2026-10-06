package com.example.mybatisdemo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.Emp;
import com.example.mapper.EmpMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/emp")
public class EmpController {

    @Autowired
    private EmpMapper empMapper;

    @GetMapping("/list")
    public List<Emp> list() {
        QueryWrapper<Emp> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("emp_id");
        return empMapper.selectList(wrapper);
    }

    @GetMapping("/test")
    public String test() {
        return "EmpController 已就绪, 共 " + empMapper.selectCount(null) + " 名员工";
    }
}