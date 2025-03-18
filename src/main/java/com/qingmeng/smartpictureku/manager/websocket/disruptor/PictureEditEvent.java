package com.qingmeng.smartpictureku.manager.websocket.disruptor;

import com.qingmeng.smartpictureku.manager.websocket.model.PictureEditRequestMessage;
import com.qingmeng.smartpictureku.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * &#064;description: disruptor 图片编辑事件
 * 充当上下文容器,所有处理消息所需要的数据都存放在这里
 *
 * @author Wang
 * &#064;date: 2025/3/17
 */
@Data
public class PictureEditEvent {

    /**
     * 消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前用户的 session
     */
    private WebSocketSession session;

    /**
     * 当前用户
     */
    private User user;

    /**
     * 图片 id
     */
    private Long pictureId;
}
