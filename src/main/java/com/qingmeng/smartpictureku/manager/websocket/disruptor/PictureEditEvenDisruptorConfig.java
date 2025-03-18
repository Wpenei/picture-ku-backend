package com.qingmeng.smartpictureku.manager.websocket.disruptor;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * &#064;description: 图片编辑事件 Disruptor 配置
 *
 * @author Wang
 * &#064;date: 2025/3/18
 */
@Configuration
public class PictureEditEvenDisruptorConfig {

    // 注入 PictureEditEventWorkHandler 实例
    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;

    /**
     * 定义一个名为 "pictureEditEventDisruptor" 的 Bean
     *
     * @return 配置好的 Disruptor 实例
     */
    @Bean("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> messageModelRingBufferDisruptor() {
        // 定义环形数组大小，大小为 1024 * 256
        int bufferSize = 1024 * 256;
        // 创建 Disruptor 实例
        Disruptor<PictureEditEvent> pictureEditEventDisruptor = new Disruptor<>(
                // 使用 lambda 表达式创建事件对象
                PictureEditEvent::new,
                // 设置环形数组大小
                bufferSize,
                // 使用 ThreadFactoryBuilder 创建线程工厂，并设置线程名称前缀
                ThreadFactoryBuilder.create().setNamePrefix("pictureEditEventDisruptor").build()
        );
        // 设置消费者，使用工作池模式处理事件
        pictureEditEventDisruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler);
        // 启动 disruptor
        pictureEditEventDisruptor.start();
        return pictureEditEventDisruptor;

    }
}
