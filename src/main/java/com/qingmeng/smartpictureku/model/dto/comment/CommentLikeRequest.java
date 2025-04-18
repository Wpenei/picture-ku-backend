package com.qingmeng.smartpictureku.model.dto.comment;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: 评论点赞请求
 *
 * @author Wang
 * &#064;date: 2025/4/18
 */
@Data
public class CommentLikeRequest implements Serializable {

    /**
     * 评论id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     *   点赞评论
     */
    private Long likeCount;

    /**
     *  踩评论
     */
    private Long dislikeCount;

    private static final long serialVersionUID = 724272877946501103L;
}
