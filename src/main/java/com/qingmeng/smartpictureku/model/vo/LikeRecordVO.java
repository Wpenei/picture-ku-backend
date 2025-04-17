package com.qingmeng.smartpictureku.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 通用点赞表响应对象
 * @author Wang
 */
@Data
public class LikeRecordVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 最近一次点赞时间
     */
    private Date lastLikeTime;

    /**
     * 点赞用户信息
     */
    private UserVO user;

    /**
     * 内容类型：1-图片 2-帖子 3-空间
     */
    private Integer targetType;

    /**
     * 被点赞内容所属用户ID
     */
    private Long targetUserId;

    /**
     * 被点赞的内容（根据targetType可能是PictureVO/Post/SpaceVO）
     */
    private Object target;

    private static final long serialVersionUID = 7665185196844867969L;
}