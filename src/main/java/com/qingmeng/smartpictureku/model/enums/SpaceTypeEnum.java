package com.qingmeng.smartpictureku.model.enums;

import cn.hutool.core.util.ObjUtil;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import lombok.Getter;

/**
 * &#064;description: 空间类型枚举

 * @author Wang
 * &#064;date: 2025/3/1 19:46
 * &#064;version: 1.0
 */
@Getter
public enum SpaceTypeEnum {

    PRIVATE("私有空间",0),
    TEAM("团队空间",1);

    private final String text;

    private final int value;

    SpaceTypeEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static SpaceTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceTypeEnum anEnum : SpaceTypeEnum.values()) {
            if (anEnum.value == value) {
                return anEnum;
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间类型不存在");
    }
}

