package com.qingmeng.smartpictureku.manager.auth;

import com.qingmeng.smartpictureku.model.entity.Picture;
import com.qingmeng.smartpictureku.model.entity.Space;
import com.qingmeng.smartpictureku.model.entity.SpaceUser;
import lombok.Data;

/**
 * &#064;description: 用户在特定空间内的授权上下文,包括管理的图片,空间,用户信息
 *
 * @author Wang
 * &#064;date: 2025/3/15
 */
@Data
public class SpaceUserAuthContext {

    /**
     * 临时参数,不同请求的id可能不同
     */
    private Long id;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 空间用户id
     */
    private Long spaceUserId;

    /**
     * 图片id
     */
    private Long pictureId;

    /**
     * 空间信息
     */
    private Space space;

    /**
     * 图片信息
     */
    private Picture picture;

    /**
     * 空间用户信息
     */
    private SpaceUser spaceUser;
}
