package com.example.demo;

import com.example.demo.step.service.StepService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * mongodb windows cmd启动命令：mongod --dbpath D:\database\mongodb\data
 * todo 本项目demo 实现高并发场景下的计步排名，即查询当日总步数在前200名的用户总步数。
 */
@SpringBootApplication
@ComponentScan({"springfox", "com.example.demo"})
@MapperScan(basePackages = "com.example.demo.*.dao.mapper")
@EnableScheduling
public class StepsApplication {

    @Resource
    private StepService stepService;
    private static StepService service;

    @PostConstruct
    public void init() {
        service = this.stepService;
    }

    public static void main(String[] args) {
        SpringApplication.run(StepsApplication.class, args);
        //启动项目初始化排名 -- redis缓存第200名的总步数，并初始化mongodb集合的数据
        service.recordTopAll(32);
    }
}
