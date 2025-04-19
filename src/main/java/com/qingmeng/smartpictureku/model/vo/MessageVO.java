package com.qingmeng.smartpictureku.model.vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 留言板表
 * @TableName message
 */
@Data
public class MessageVO implements Serializable {
    /**
     * 主键ID
     */
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
     * IP地址
     */
    private String ip;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}