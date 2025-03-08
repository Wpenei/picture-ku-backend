package com.qingmeng.smartpictureku.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: TODO

 * @author Wang
 * &#064;date: 2025/3/1
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3330280410710414130L;
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
