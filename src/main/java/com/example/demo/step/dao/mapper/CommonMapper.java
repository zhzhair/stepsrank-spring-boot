package com.example.demo.step.dao.mapper;

import com.example.demo.step.documents.StepsTop;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommonMapper {

    void createTable(@Param("tableName") String tableName);

    void dropTableIfExists(@Param("tableName") String tableName);

    Integer getStepCount(@Param("tableName") String tableName, @Param("userId") Integer userId);

    void updateSteps(@Param("tableName") String tableName, @Param("userId") Integer userId, @Param("stepCount") Integer stepCount);

    void insertTables(@Param("tableName") String tableName, @Param("userId") Integer userId, @Param("stepCount") Integer stepCount);

    List<StepsTop> getTopStep(@Param("tableName") String tableName);
}