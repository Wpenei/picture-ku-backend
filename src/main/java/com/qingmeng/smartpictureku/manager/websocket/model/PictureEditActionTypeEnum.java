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
public enum PictureEditActionTypeEnum {


    ZOOM_IN("放大操作", "ZOOM_IN"),
    ZOOM_OUT("缩小操作", "ZOOM_OUT"),
    ROTATE_LEFT("左旋操作", "ROTATE_LEFT"),
    ROTATE_RIGHT("右旋操作", "ROTATE_RIGHT");

    private final String text;

    private final String value;

    PictureEditActionTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static PictureEditActionTypeEnum getEnumByValue(String value) {
        if (value == null || ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PictureEditActionTypeEnum anEnum : PictureEditActionTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}

