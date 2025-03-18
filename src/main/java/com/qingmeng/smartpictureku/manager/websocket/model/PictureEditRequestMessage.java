package com.qingmeng.smartpictureku.manager.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * &#064;description: 图片编辑请求消息 接收前端的信息
 *
 * @author Wang
 * &#064;date: 2025/3/17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PictureEditRequestMessage {

    /**
     * 消息类型 例如 进入编辑状态"ENTER_EDIT", 退出编辑状态"EXIT_EDIT", 执行编辑"EDIT_ACTION"
     */
    private String type;

    /**
     * 执行的编辑操作
     */
    private String editAction;
}
