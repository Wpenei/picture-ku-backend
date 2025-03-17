package com.qingmeng.smartpictureku.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * &#064;description: 用户视图
 * @author Wang
 * &#064;date: 2025/3/2
 */
@Data
public class UserVO implements Serializable {
    private static final long serialVersionUID = -774043825217443818L;
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

    /**
     * 创建时间
     */
    private Date createTime;

}
