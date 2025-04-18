package com.qingmeng.smartpictureku.model.vo;

import com.qingmeng.smartpictureku.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 评论VO
 * @author Wang
 * @TableName comments
 */
@Data
public class CommentVO extends PageRequest implements Serializable {
    /**
     * 评论id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 评论目标ID
     */
    private Long targetId;

    /**
     * 评论目标类型：1-图片 2-帖子
     */
    private Integer targetType;

    /**
     * 评论目标所属用户ID
     */
    private Long targetUserId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID,0表示顶级
     */
    private Long parentCommentId;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 点踩数
     */
    private Long dislikeCount;

    /**
     * 评论数
     */
    private Long commentCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 评论用户信息
     */
    private CommentUserVO commentUser;

    /**
     * 图片信息（当 targetType = 1 时）
     */
    private PictureVO picture;

    /**
     * 帖子信息（当 targetType = 2 时）
     */
    //private Post post;

    /**
     * 子评论列表
     */
    private List<CommentVO> children;

    private static final long serialVersionUID = 1L;


}