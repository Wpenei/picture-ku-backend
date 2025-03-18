package com.qingmeng.smartpictureku.manager.websocket.model;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * &#064;description: 图片编辑消息类型枚举

 * @author Wang
 * &#064;date: 2025/3/1 19:46
 * &#064;version: 1.0
 */
@Getter
public enum PictureEditMessageTypeEnum {

    INFO("发送通知","INFO"),
    ERROR("错误通知","ERROR"),
    ENTER_EDIT("进入编辑状态","ENTER_EDIT"),
    EXIT_EDIT("退出编辑状态","EXIT_EDIT"),
    EDIT_ACTION("执行编辑操作","EDIT_ACTION");

    private final String text;

    private final String value;

    PictureEditMessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static PictureEditMessageTypeEnum getEnumByValue(String value) {
        if (value == null || ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PictureEditMessageTypeEnum anEnum : PictureEditMessageTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}

