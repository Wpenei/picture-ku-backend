package com.qingmeng.smartpictureku.model.enums;

import cn.hutool.core.util.ObjUtil;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * &#064;description: 空间角色枚举

 * @author Wang
 * &#064;date: 2025/3/1 19:46
 * &#064;version: 1.0
 */
@Getter
public enum SpaceRoleEnum {

    VIEWER("浏览者",0),
    EDITOR("编辑者",1),
    ADMINER("管理员",2);

    private final String text;

    private final int value;

    SpaceRoleEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static SpaceRoleEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceRoleEnum anEnum : SpaceRoleEnum.values()) {
            if (anEnum.value == value) {
                return anEnum;
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间类型不存在");
    }


    /**
     * 获取所有枚举的文本列表
     *
     * @return 文本列表
     */
    public static List<String> getAllTexts() {
        return Arrays.stream(SpaceRoleEnum.values())
                .map(SpaceRoleEnum::getText)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有枚举的值列表
     * @return 值列表
     */
    public static List<Integer> getAllValues() {
        return Arrays.stream(SpaceRoleEnum.values())
                .map(SpaceRoleEnum::getValue)
                .collect(Collectors.toList());
    }
}

