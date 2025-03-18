package com.qingmeng.smartpictureku.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.qingmeng.smartpictureku.manager.auth.model.SpaceUserAuthConfig;
import com.qingmeng.smartpictureku.manager.auth.model.SpaceUserRole;
import com.qingmeng.smartpictureku.model.entity.Space;
import com.qingmeng.smartpictureku.model.entity.SpaceUser;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.enums.SpaceRoleEnum;
import com.qingmeng.smartpictureku.model.enums.SpaceTypeEnum;
import com.qingmeng.smartpictureku.service.SpaceUserService;
import com.qingmeng.smartpictureku.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * &#064;description: 加载配置文件到对象
 *
 * @author Wang
 * &#064;date: 2025/3/15
 */
@Component
public class SpaceUserAuthManage {

    @Resource
    private UserService userservice;

    @Resource
    private SpaceUserService spaceUserService;

    // 静态常量 用于存储配置文件
    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG ;

    // 静态代码块 在类加载时执行,且只执行一次,用于初始化静态变量
    static {
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json,SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     * @param spaceUserRole
     * @return
     */
    public List<String> getPermissionsByRole(Integer spaceUserRole){
        if (ObjUtil.isNull(spaceUserRole)) {
            return new ArrayList<>();
        }
        // 找到匹配的角色
        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles().stream()
                .filter(r -> r.getKey().equals(spaceUserRole))
                .findFirst()
                .orElse(null);
        if (ObjUtil.isNull(role)) {
            return new ArrayList<>();
        }
        return role.getPermissions();
    }

    /**
     * 获取当前登录用户在该空间中的权限列表
     */
    public List<String> getPermissionsList(Space space, User loginUser){
        //
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 管理员权限
        List<String> ADMIN_PERMISSION = getPermissionsByRole(SpaceRoleEnum.ADMINER.getValue());
        // 如果是管理员,应返回全部权限
        if (userservice.isAdmin(loginUser)){
            return ADMIN_PERMISSION;
        }
        // 公共图库
        if(space == null){
            // 如果是管理员,返回所有权限
            if (userservice.isAdmin(loginUser)){
                return ADMIN_PERMISSION;
            }
            return new ArrayList<>();
        }
        // 根据空间类型获取空间类型枚举
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null){
            return new ArrayList<>();
        }
        // 不同类型的空间,校验方法不一致
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间 需要校验该空间是否为当前登录用户的
                if (space.getUserId().equals(loginUser.getId()) || userservice.isAdmin(loginUser)) {
                    return ADMIN_PERMISSION;
                }
                return new ArrayList<>();
            case TEAM:
                // 团队空间 查询SpaceUser 并获取用户角色
                SpaceUser one = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (one == null) {
                    return new ArrayList<>();
                }else {
                    return getPermissionsByRole(one.getSpaceRole());
                }
        }
        return new ArrayList<>();
    }
}
