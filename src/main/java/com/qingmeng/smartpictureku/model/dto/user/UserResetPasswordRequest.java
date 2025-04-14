package com.qingmeng.smartpictureku.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: 忘记密码

 * @author Wang
 * &#064;date: 2025/3/1
 */
@Data
public class UserResetPasswordRequest implements Serializable {

    private static final long serialVersionUID = 3330280410710414130L;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String code;

    /**
     * 密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
