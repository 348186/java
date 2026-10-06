package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Emp;
import org.apache.ibatis.annotations.Param;

public interface EmpMapper extends BaseMapper<Emp> {

    int insertEmp(Emp emp);

    Emp selectByIdWithMap(@Param("id") Integer id);
}