package com.qingmeng.smartpictureku.model.dto.comment;

import com.qingmeng.smartpictureku.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: 评论查询请求
 *
 * @author Wang
 * &#064;date: 2025/4/18
 */
@Data
public class CommentQueryRequest extends PageRequest implements Serializable {


    /**
     * 评论目标ID
     */
    private Long targetId;

    /**
     * 评论目标类型：1-图片 2-帖子 默认为1
     */
    private Integer targetType;

    private static final long serialVersionUID = 724272877946501103L;
}
