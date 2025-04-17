package com.qingmeng.smartpictureku.model.dto.category;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: 添加分类请求
 *
 * @author Wang
 * &#064;date: 2025/4/17
 */
@Data
public class CategoryAddRequest implements Serializable {

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类类型
     */
    private Integer type;

    private static final long serialVersionUID = 724272877946501103L;
}
