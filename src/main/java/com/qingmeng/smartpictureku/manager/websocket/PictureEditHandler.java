package com.qingmeng.smartpictureku.manager.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.qingmeng.smartpictureku.manager.websocket.disruptor.PictureEditEventProducer;
import com.qingmeng.smartpictureku.manager.websocket.model.PictureEditActionTypeEnum;
import com.qingmeng.smartpictureku.manager.websocket.model.PictureEditMessageTypeEnum;
import com.qingmeng.smartpictureku.manager.websocket.model.PictureEditRequestMessage;
import com.qingmeng.smartpictureku.manager.websocket.model.PictureEditResponseMessage;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * &#064;description: 图片编辑 WebSocket 处理器
 *
 * @author Wang
 * &#064;date: 2025/3/17
 */
@Component
@Slf4j
public class PictureEditHandler extends TextWebSocketHandler {

    // 接收每张图片的编辑状态 key == pictureId value == 正在编辑的用户id userId
    private final Map<Long, Long> pictureEditStatusMap = new ConcurrentHashMap<>();

    // 存放所有连接的会话,key == pictureId ,value == 用户会话集合
    private final Map<Long, Set<WebSocketSession>> pictureEditSessionMap = new ConcurrentHashMap<>();

    /**
     * todo 某张图片的编辑记录，key: pictureId, value: 编辑记录，用于某个用户中途加入编辑的时候看到最新的编辑记录
     */
    //private final Map<Long, List<TextMessage>> pictureEditRecodes = new ConcurrentHashMap<>();

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private PictureEditEventProducer pictureEditEventProducer;

    public PictureEditHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * 建立连接成功
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        // 保存会话到集合中
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        // 如果是首次加入连接,需要初始化一个集合,并将信息添加到集合中
        pictureEditSessionMap.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        pictureEditSessionMap.get(pictureId).add(session);
        // 构造响应发送加入编辑的消息
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        pictureEditResponseMessage.setMessage(String.format(" %s 加入编辑", user.getUserName()));
        // 广播给当前图片的所有会话用户
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }

    /**
     * 接收到前端发送的消息,将消息按类别处理
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        // 将JSON转换为 对象
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        String type = pictureEditRequestMessage.getType();
        // 根据类型获取消息类型枚举
        PictureEditMessageTypeEnum messageTypeEnum = PictureEditMessageTypeEnum.getEnumByValue(type);

        // 从session中获取公共参数
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");

        // 使用Disruptor 异步处理
        pictureEditEventProducer.publishEvent(pictureEditRequestMessage, user, pictureId, session);
    }

    /**
     * 关闭连接之后
     *
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        // 从session中获取公共参数
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        // 移除当前用户的编辑状态
        handleExitEditMessage(null, user, pictureId, session);
        // 删除会话
        Set<WebSocketSession> sessionSet = pictureEditSessionMap.get(pictureId);
        if(sessionSet != null){
            sessionSet.remove(session);
            if (sessionSet.isEmpty()){
                pictureEditSessionMap.remove(pictureId);
            }
        }
        // 通知其他用户该用户已退出
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        pictureEditResponseMessage.setMessage(String.format(" %s 离开编辑", user.getUserName()));
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }

    /**
     * 广播给该图片的所有会话用户(可以排除掉某个Session)
     *
     * @param pictureId                  图片id
     * @param pictureEditResponseMessage 图片编辑响应消息
     * @param excludeSession             排除的会话用户
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage,
                                    WebSocketSession excludeSession) throws IOException {
        // 获取到当前图片的所有会话用户
        Set<WebSocketSession> webSocketSessions = pictureEditSessionMap.get(pictureId);
        if (CollUtil.isNotEmpty(webSocketSessions)) {
            // 创建 ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            // 配置序列化：将 Long 类型转为 String，解决丢失精度问题
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            // 支持 long 基本类型
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            objectMapper.registerModule(module);
            // 序列化为 JSON 字符串
            String message = objectMapper.writeValueAsString(pictureEditResponseMessage);
            // 定义消息
            TextMessage textMessage = new TextMessage(message);
            // 发送消息
            for (WebSocketSession session : webSocketSessions) {
                // 不向排除掉的用户发送
                if (session.equals(excludeSession)) {
                    continue;
                }
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }

    }

    /**
     * 广播给全部会话用户
     *
     * @param pictureId                  图片id
     * @param pictureEditResponseMessage 图片编辑响应消息
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws IOException {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }

    /**
     * 进入编辑状态消息
     * @param pictureEditRequestMessage
     * @param user
     * @param pictureId
     * @param session
     */
    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage, User user, Long pictureId, WebSocketSession session) throws IOException {
        // 要判断当前没有用户在编辑状态
        if (!pictureEditStatusMap.containsKey(pictureId)){
            // 设置当前用户为编辑用户
            pictureEditStatusMap.put(pictureId,user.getId());
            // 构造响应,发送加入编辑的消息通知
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            pictureEditResponseMessage.setMessage(String.format(" %s 开始编辑图片", user.getUserName()));
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            // 广播给当前图片的所有会话用户
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    /**
     * 处理编辑操作
     * @param pictureEditRequestMessage
     * @param user
     * @param pictureId
     * @param session
     */
    public void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage, User user, Long pictureId, WebSocketSession session) throws IOException {
        // 正在编辑的用户
        Long userId = pictureEditStatusMap.get(pictureId);
        // 执行的编辑操作
        String editAction = pictureEditRequestMessage.getEditAction();
        // 根据编辑操作获取编辑操作枚举
        PictureEditActionTypeEnum editActionEnum = PictureEditActionTypeEnum.getEnumByValue(editAction);
        if (editActionEnum == null){
            log.error("无效的编辑动作");
            return;
        }
        // 确认是当前的编辑者
        if (userId.equals(user.getId())){
            // 构造响应消息
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            pictureEditResponseMessage.setMessage(String.format(" %s 执行了 %s", user.getUserName(),editActionEnum.getText()));
            pictureEditResponseMessage.setEditAction(editAction);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            // 广播给除了自己以外的用户,否则会重复编辑
            broadcastToPicture(pictureId, pictureEditResponseMessage,session);

        }
    }

    /**
     * 退出编辑状态消息
     * @param pictureEditRequestMessage
     * @param user
     * @param pictureId
     * @param session
     */
    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage, User user, Long pictureId, WebSocketSession session) throws IOException {
        // 正在编辑的用户id
        Long userId = pictureEditStatusMap.get(pictureId);
        if (userId != null &&  userId.equals(user.getId())){
            // 移除正在编辑用户
            pictureEditStatusMap.remove(pictureId);

            // 构造响应,发送加入编辑的消息通知
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            pictureEditResponseMessage.setMessage(String.format(" %s 退出编辑图片", user.getUserName()));
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            // 广播给当前图片的所有会话用户
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }


}
