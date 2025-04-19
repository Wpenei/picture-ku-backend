package com.qingmeng.smartpictureku.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: 图片标签分类响应对象
 *
 * @author Wang
 * &#064;date: 2025/3/6
 */
@Data
public class MessageCenterVO implements Serializable {
    /**
     * 未读消息总数
     */
    private long totalUnread;

    /**
     * 未读评论数
     */
    private long unreadComments;

    /**
     * 未读点赞数
     */
    private long unreadLikes;

    /**
     * todo 未读分享数 没有添加分享表
     */
    //private long unreadShares;

    private static final long serialVersionUID = 5676454347671641287L;
}
