package com.qingmeng.smartpictureku.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingmeng.smartpictureku.model.dto.spaceuser.SpaceUserAddRequest;
import com.qingmeng.smartpictureku.model.dto.spaceuser.SpaceUserQueryRequest;
import com.qingmeng.smartpictureku.model.entity.SpaceUser;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.vo.SpaceUserVO;

import java.util.List;

/**
 * @author Wang
 * @description 针对表【space_user(空间用户关联表)】的数据库操作Service
 * @createDate 2025-03-15 15:08:57
 */
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间用户
     *
     * @param spaceUserAddRequest
     * @param loginUser
     * @return SpaceUser.id
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest, User loginUser);

    /**
     * 校验空间用户
     *
     * @param spaceUser
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 获取查询条件
     *
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取单个空间用户封装对象SpaceUserVO
     *
     * @param spaceUser
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser);

    /**
     * 获取封装对象列表
     *
     * @param spaceUserList
     * @return
     */
    List<SpaceUserVO> getSpaceUserVoList(List<SpaceUser> spaceUserList);

    /**
     * 查询用户加入的团队空间（排除用户自己创建的）
     */
    List<SpaceUser> listMyCreateSpace(Long userId);

    /**
     * 查询用户加入的团队空间（排除用户自己创建的）
     */
    List<SpaceUser> listMyJoinSpace(Long userId);

}
