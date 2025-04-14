package com.qingmeng.smartpictureku.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: 修改密码请求

 * @author Wang
 * &#064;date: 2025/3/1
 */
@Data
public class UserModifyPassWordRequest implements Serializable {

    private static final long serialVersionUID = 3330280410710414130L;

    /**
     * 邮箱
     */
    private Long id;

    /**
     * 原密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
