package com.qingmeng.smartpictureku.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: 用户登录请求
 * @author Wang
 * &#064;date: 2025/3/1
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = -864896990859542588L;

    /**
     *  邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 验证码
     */
    private String inputVerifyCode;

    /**
     * 验证码ID
     */
    private String serverVerifyCode;
}
