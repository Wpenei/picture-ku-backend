package com.qingmeng.smartpictureku.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: 评论用户VO
 * @author Wang
 * &#064;date: 2025/3/2
 */
@Data
public class CommentUserVO implements Serializable {
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

}
