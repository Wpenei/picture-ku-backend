package com.qingmeng.smartpictureku.model.dto.user;

import lombok.Getter;

import java.io.Serializable;

/**
 * &#064;description: 用户登录请求
 * @author Wang
 * &#064;date: 2025/3/1
 */
@Getter
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = -864896990859542588L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;
}
