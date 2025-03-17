package com.qingmeng.smartpictureku.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: 空间成员添加请求
 *
 * @author Wang
 * &#064;date: 2025/3/15
 */
@Data
public class SpaceUserAddRequest implements Serializable {

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 空间用户角色 0-浏览者 1-编辑者 2-管理员
     */
    private Integer spaceRole;

    private static final long serialVersionUID = 7854798484175011205L;
}
