package com.qingmeng.smartpictureku.model.dto.like;

import com.qingmeng.smartpictureku.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * &#064;description: 查询点赞请求
 *
 * @author Wang
 * &#064;date: 2025/4/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LikeQueryRequest extends PageRequest implements Serializable {
    /**
     * 目标类型：1-图片 2-帖子
     */
    private Integer targetType;


    private static final long serialVersionUID = 1L;
}
