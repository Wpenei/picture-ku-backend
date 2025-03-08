package com.qingmeng.smartpictureku.model.dto.user;

import lombok.Getter;

import java.io.Serializable;

/**
 * &#064;description: 创建用户请求
 *
 * @author Wang
 * &#064;date: 2025/3/2
 */
@Getter
public class UserAddRequest implements Serializable {

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    private static final long serialVersionUID = 724272877946501103L;
}
