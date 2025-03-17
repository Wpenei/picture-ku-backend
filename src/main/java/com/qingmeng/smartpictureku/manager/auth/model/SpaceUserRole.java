package com.qingmeng.smartpictureku.manager.auth.model;

import lombok.Data;

import java.util.List;

/**
 * &#064;description: 空间用户角色配置
 *
 * @author Wang
 * &#064;date: 2025/3/15
 */
@Data
public class SpaceUserRole {

    /**
     * 角色key todo 如果有问题修改为String
     */
    private Integer key;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色权限列表
     */
    private List<String> permissions;

    /**
     * 角色描述
     */
    private String description;
}
