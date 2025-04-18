package com.qingmeng.smartpictureku.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 分类
 * @TableName category
 */
@TableName(value ="category")
@Data
public class Category implements Serializable {
    /**
     * 分类id
     */
    @TableId(type = IdType.ASSIGN_ID)
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

    /**
     * 是否删除
     */
    @TableLogic // 逻辑删除
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}