package com.qingmeng.smartpictureku.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * &#064;description: 标签响应对象
 * @author Wang
 * &#064;date: 2025/3/2
 */
@Data
public class CategoryVO implements Serializable {
    private static final long serialVersionUID = -774043825217443818L;
    /**
     * 分类id
     */
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类类型：0-图片分类 1-帖子分类
     */
    private Integer categoryType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 分类编辑时间
     */
    private Date editTime;

    /**
     * 分类更新时间
     */
    private Date updateTime;

}
