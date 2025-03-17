package com.qingmeng.smartpictureku.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * &#064;description: 图片状态枚举

 * @author Wang
 * &#064;date: 2025/3/1 19:46
 * &#064;version: 1.0
 */
@Getter
public enum PictureReviewStatusEnum {

    REVIEWING("审核中", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);

    private final String text;

    private final int value;

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PictureReviewStatusEnum pictureReviewStatusEnum : PictureReviewStatusEnum.values()) {
            if (pictureReviewStatusEnum.value == value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }
}

