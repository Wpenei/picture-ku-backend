package com.qingmeng.smartpictureku.model.dto.user;

import lombok.Getter;

import java.io.Serializable;

/**
 * &#064;description: 更新用户请求
 *
 * @author Wang
 * &#064;date: 2025/3/2
 */
@Getter
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;


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
