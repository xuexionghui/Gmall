package com.atguigu.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * Created by 辉 on 2020/3/21.
 */
@SpringBootApplication
@MapperScan(basePackages = {"com.atguigu.gmall.mapper"})
public class GmallUserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallUserServiceApplication.class);
    }
}
