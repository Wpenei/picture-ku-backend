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
public class TagVO implements Serializable {
    private static final long serialVersionUID = -774043825217443818L;
    /**
     * id
     */
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
