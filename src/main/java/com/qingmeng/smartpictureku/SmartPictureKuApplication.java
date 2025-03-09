package com.qingmeng.smartpictureku;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.qingmeng.smartpictureku.mapper")
@EnableAspectJAutoProxy(exposeProxy = true) // 开启AOP
public class SmartPictureKuApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartPictureKuApplication.class, args);
    }

}
