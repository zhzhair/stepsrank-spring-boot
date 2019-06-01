package com.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @Test
    public void contextLoads() {
        System.err.println("===========================begin========================");
        long begin = System.currentTimeMillis();
        System.out.println("耗时（毫秒）：" + (System.currentTimeMillis() - begin));
        System.err.println("============================end=========================");
    }

}
