package com.qingmeng.smartpictureku.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * &#064;description: 空间用户角色权限配置
 *
 * @author Wang
 * &#064;date: 2025/3/15
 */
@Data
public class SpaceUserAuthConfig implements Serializable {


    /**
     * 权限列表
     */
    private List<SpaceUserPermission> permissions;

    /**
     * 角色列表
     */
    private List<SpaceUserRole> roles;


    private static final long serialVersionUID = -5356170588250825083L;
}
