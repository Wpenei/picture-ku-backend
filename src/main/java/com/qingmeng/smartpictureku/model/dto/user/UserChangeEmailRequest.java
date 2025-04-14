package com.qingmeng.smartpictureku.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: 更换绑定邮箱请求
 *
 * @author Wang
 * &#064;date: 2025/4/11 UserChangeEmailRequest
 */
@Data
public class UserChangeEmailRequest implements Serializable {
    private static final long serialVersionUID = -4825607894781718323L;
    /**
     * 新的邮箱地址
     */
    private String newEmail;

    /**
     * 验证码
     */
    private String code;

}
