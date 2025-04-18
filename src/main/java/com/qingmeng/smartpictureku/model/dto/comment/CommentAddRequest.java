package com.qingmeng.smartpictureku.model.dto.comment;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: 创建评论请求
 *
 * @author Wang
 * &#064;date: 2025/4/18
 */
@Data
public class CommentAddRequest implements Serializable {

    /**
     * 评论目标ID
     */
    private Long targetId;

    /**
     * 评论目标类型：1-图片 2-帖子 3-评论 默认为1
     */
    private Integer targetType =1;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID,0表示顶级
     */
    //private Long parentCommentId;

    private static final long serialVersionUID = 724272877946501103L;
}
