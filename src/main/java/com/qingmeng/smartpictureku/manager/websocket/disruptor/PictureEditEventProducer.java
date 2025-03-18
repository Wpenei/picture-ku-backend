package com.qingmeng.smartpictureku.manager.websocket.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.qingmeng.smartpictureku.manager.websocket.model.PictureEditRequestMessage;
import com.qingmeng.smartpictureku.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * &#064;description: 图片编辑事件 生产者
 * 负责将数据发到Disruptor 环形缓冲区中
 *
 * @author Wang
 * &#064;date: 2025/3/18
 */
@Component
@Slf4j
public class PictureEditEventProducer {

    @Resource
    Disruptor<PictureEditEvent> pictureEditEventDisruptor;

    /**
     * 发布图片编辑事件到Disruptor环形缓冲区
     *
     * @param pictureEditRequestMessage 图片编辑请求消息
     * @param user                      用户信息
     * @param pictureId                 图片ID
     * @param session                   WebSocket会话
     */
    public void publishEvent(PictureEditRequestMessage pictureEditRequestMessage, User user, Long pictureId, WebSocketSession session) {
        // 获取Disruptor的环形缓冲区
        RingBuffer<PictureEditEvent> ringBuffer = pictureEditEventDisruptor.getRingBuffer();
        // 获取到可以放置事件的位置
        long next = ringBuffer.next();
        try {
            // 获取环形缓冲区中的事件对象
            PictureEditEvent pictureEditEvent = ringBuffer.get(next);
            // 设置事件对象的属性
            pictureEditEvent.setPictureEditRequestMessage(pictureEditRequestMessage);
            pictureEditEvent.setSession(session);
            pictureEditEvent.setUser(user);
            pictureEditEvent.setPictureId(pictureId);

        } finally {
            // 发布事件
            ringBuffer.publish(next);

        }
    }
    /**
     * 优雅停机
     */
    @PreDestroy
    public void shutdown () {
        pictureEditEventDisruptor.shutdown();
    }
}