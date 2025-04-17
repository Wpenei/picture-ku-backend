package com.qingmeng.smartpictureku;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})// 关闭分库分表
@EnableAsync
@MapperScan("com.qingmeng.smartpictureku.mapper")
@EnableAspectJAutoProxy(exposeProxy = true) // 开启AOP
public class SmartPictureKuApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartPictureKuApplication.class, args);
    }

}
