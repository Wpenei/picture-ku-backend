package com.qingmeng.smartpictureku.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 留言板表
 * @TableName message
 */
@TableName(value ="message")
@Data
public class Message implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 留言内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除(0-未删除 1-已删除)
     */
    @TableLogic
    private Integer isDelete;

    /**
     * IP地址
     */
    private String ip;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}