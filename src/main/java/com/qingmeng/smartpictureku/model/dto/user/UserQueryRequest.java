package com.qingmeng.smartpictureku.model.dto.user;

import com.qingmeng.smartpictureku.common.PageRequest;
import lombok.Getter;

import java.io.Serializable;

/**
 * &#064;description: 查询用户请求
 *
 * @author Wang
 * &#064;date: 2025/3/2
 */
@Getter
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;


    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    private static final long serialVersionUID = -6980040477527061802L;
}
