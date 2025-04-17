package com.qingmeng.smartpictureku.model.dto.like;

import lombok.Data;

/**
 * &#064;description: 点赞请求
 *
 * @author Wang
 * &#064;date: 2025/4/17
 */
@Data
public class LikeRequest {
    /**
     * 目标内容ID
     */
    private Long targetId;

    /**
     * 内容类型：1-图片 2-帖子 3-空间
     */
    private Integer targetType;

    /**
     * 是否点赞
     */
    private Boolean isLiked;
}
