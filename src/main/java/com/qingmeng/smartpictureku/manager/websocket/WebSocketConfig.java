package com.qingmeng.smartpictureku.manager.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

/**
 * &#064;description: WebSocket 配置类, 注册自定义的拦截器和处理器
 *
 * @author Wang
 * &#064;date: 2025/3/17
 */
@Component
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    // 拦截器
    @Resource
    private WebSocketInterceptor webSocketInterceptor;

    // 处理器
    @Resource
    private PictureEditHandler pictureEditHandler;
    /**
     * 注册WebSocket处理器和拦截器
     * @param registry WebSocket处理器注册表
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        // websocket 路径映射
        // 将pictureEditHandler处理器映射到/webs/picture/edit路径
        registry.addHandler(pictureEditHandler, "/ws/picture/edit")
                // 添加webSocketInterceptor拦截器到该路径
                .addInterceptors(webSocketInterceptor)
                //  允许所有来源的请求
                .setAllowedOrigins("*");
    }
}
