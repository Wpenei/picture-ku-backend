package com.qingmeng.smartpictureku;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author Wang
 */
@EnableAsync
@EnableAspectJAutoProxy(exposeProxy = true) // 开启AOP
@MapperScan("com.qingmeng.smartpictureku.mapper")
@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})// 关闭分库分表
public class SmartPictureKuApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartPictureKuApplication.class, args);
    }

}
