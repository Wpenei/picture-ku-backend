package com.qingmeng.smartpictureku.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * &#064;description: TODO

 * @author Wang
 * &#064;date: 2025/3/1 19:46
 * &#064;version: 1.0
 */
@Getter
public enum UserRoleEnum {

    USER("普通用户","user"),
    VIP("VIP用户","vip"),
    ADMIN("管理员","admin");

    private final String text;

    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}

