package com.qingmeng.smartpictureku.model.dto.message;

import lombok.Data;

import java.io.Serializable;

/**
 * 添加留言
 * @author Wang
 * @TableName message
 */
@Data
public class MessageAddRequest implements Serializable {

    /**
     * 留言内容
     */
    private String content;

    /**
     * IP地址
     */
    private String ip;

    private static final long serialVersionUID = 1L;
}