package com.qingmeng.smartpictureku.manager.auth.model;

import lombok.Data;

/**
 * &#064;description: 空间用户权限
 *
 * @author Wang
 * &#064;date: 2025/3/15
 */
@Data
public class SpaceUserPermission {

    /**
     * 权限key
     */
    private String key;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限描述
     */
    private String description;

}
