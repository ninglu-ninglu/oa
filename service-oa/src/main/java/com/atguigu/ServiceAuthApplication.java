package com.atguigu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/*
@time 2023/8/14-15:19
@authon cheny
@name 哈哈
@version 1.0
*/
@SpringBootApplication
//@MapperScan("com.atguigu.auth.mapper")
//@ComponentScan("com.atguigu")
public class ServiceAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceAuthApplication.class,args);

    }
}
