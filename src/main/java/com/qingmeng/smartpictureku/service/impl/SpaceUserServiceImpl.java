package com.qingmeng.smartpictureku.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.manager.auth.SpaceUserAuthManage;
import com.qingmeng.smartpictureku.mapper.SpaceUserMapper;
import com.qingmeng.smartpictureku.model.dto.spaceuser.SpaceUserAddRequest;
import com.qingmeng.smartpictureku.model.dto.spaceuser.SpaceUserQueryRequest;
import com.qingmeng.smartpictureku.model.entity.Space;
import com.qingmeng.smartpictureku.model.entity.SpaceUser;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.enums.SpaceRoleEnum;
import com.qingmeng.smartpictureku.model.vo.SpaceUserVO;
import com.qingmeng.smartpictureku.model.vo.SpaceVO;
import com.qingmeng.smartpictureku.service.SpaceService;
import com.qingmeng.smartpictureku.service.SpaceUserService;
import com.qingmeng.smartpictureku.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Wang
* @description 针对表【space_user(空间用户关联表)】的数据库操作Service实现
* @createDate 2025-03-15 15:08:57
*/
@Service
@Slf4j
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService{

    @Resource
    @Lazy
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private SpaceUserAuthManage spaceUserAuthManage;

    /**
     * 添加空间用户
     * @param spaceUserAddRequest
     * @param loginUser
     * @return SpaceUser.id
     */
    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserAddRequest, spaceUser);
        if (spaceUser.getSpaceRole() == null){
            spaceUser.setSpaceRole(SpaceRoleEnum.VIEWER.getValue());
        }
        // 创建时校验
        validSpaceUser(spaceUser, true);
        // 保存
        boolean save = this.save(spaceUser);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR);
        return spaceUser.getId();
    }

    /**
     * 创建和编辑时校验,空间用户
     * @param spaceUser
     */
    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        if (add) {
            // 创建空间用户
            ThrowUtils.throwIf(spaceId == null || userId == null, ErrorCode.PARAMS_ERROR, "空间id或用户id不能为空");
            ThrowUtils.throwIf(userService.getById(userId) == null, ErrorCode.PARAMS_ERROR, "用户不存在");
            ThrowUtils.throwIf(spaceService.getById(spaceId) == null, ErrorCode.PARAMS_ERROR, "空间不存在");
        }
        // 校验空间角色
        Integer spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum enumByValue = SpaceRoleEnum.getEnumByValue(spaceRole);
        if (spaceRole != null && enumByValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间角色不存在");
        }

    }

    /**
     * 获取查询条件
     * @param spaceUserQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        QueryWrapper<SpaceUser> spaceUserQueryWrapper = new QueryWrapper<>();
        if (spaceUserQueryRequest == null) {
            return spaceUserQueryWrapper;
        }
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        Integer spaceRole = spaceUserQueryRequest.getSpaceRole();

        // 构造查询条件
        spaceUserQueryWrapper.lambda()
                .eq(ObjUtil.isNotEmpty(id), SpaceUser::getId, id)
                .eq(ObjUtil.isNotEmpty(userId), SpaceUser::getUserId, userId)
                .eq(ObjUtil.isNotEmpty(spaceId), SpaceUser::getSpaceId, spaceId)
                .eq(ObjUtil.isNotEmpty(spaceRole), SpaceUser::getSpaceRole, spaceRole);
        // 排序默认根据创建时间进行排序
        spaceUserQueryWrapper.lambda().orderByAsc(SpaceUser::getCreateTime);
        return spaceUserQueryWrapper;
    }

    /**
     * 获取单个空间用户封装对象SpaceUserVO
     * @param spaceUser
     * @return
     */
    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser) {
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        // 关联查询用户信息
        Long userId = spaceUserVO.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                spaceUserVO.setUserVO(userService.getUserVO(user));
            }
        }
        // 关联查询空间信息
        Long spaceId = spaceUserVO.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            if (space != null) {
                spaceUserVO.setSpaceVO(spaceService.getSpaceVO(space));
            }
        }
        return spaceUserVO;
    }

    /**
     * 获取封装对象列表
     * @param spaceUserList
     * @return
     */
    @Override
    public List<SpaceUserVO> getSpaceUserVoList(List<SpaceUser> spaceUserList) {
        // 如果spaceUserList数组为null,则返回空数组
        if (CollUtil.isEmpty(spaceUserList)) {
            log.info("spaceUserList数组为null");
            return Collections.emptyList();
        }
        // 将空间对象列表 => 封装对象列表
        List<SpaceUserVO> spaceVoList = spaceUserList.stream().map(SpaceUserVO::objToVo).collect(Collectors.toList());
        // 关联查询用户信息
        // 获取用户id集合
        Set<Long> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        Set<Long> spaceIdSet = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());
        // 批量查询用户信息
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        Map<Long, List<SpaceVO>> spaceIdSpaceListMap = spaceService.listByIds(spaceIdSet).stream()
                .map(SpaceVO::objToVo)
                .collect(Collectors.groupingBy(SpaceVO::getId));
        // 填充信息
        spaceVoList.forEach(spaceUserVO  -> {
            Long userId = spaceUserVO.getUserId();
            Long spaceId = spaceUserVO.getSpaceId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceUserVO.setUserVO(userService.getUserVO(user));
            // 填充空间信息
            SpaceVO spaceVO = null;
            if (spaceIdSpaceListMap.containsKey(spaceId)) {
                spaceVO = spaceIdSpaceListMap.get(spaceId).get(0);
                spaceVO.setUserVO(userService.getUserVO(userService.getById(spaceVO.getUserId())));
                spaceVO.setPermissionList(spaceUserAuthManage.getPermissionsByRole(spaceUserVO.getSpaceRole()));
            }
            spaceUserVO.setSpaceVO(spaceVO);
        });
        return spaceVoList;
    }

    /**
     * 查询用户加入的团队空间（排除用户自己创建的）
     * @param userId 用户id
     * @return 用户加入的团队空间列表
     */
    @Override
    public List<SpaceUser> listMyCreateSpace(Long userId){
        // 1.校验参数
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        // 2.根据用户id查询用户加入的团队空间
        return this.baseMapper.selectSpaceUserByUserIdCreate(userId);
    }
    /**
     * 查询用户加入的团队空间（排除用户自己创建的）
     * @param userId 用户id
     * @return 用户加入的团队空间列表
     */
    @Override
    public List<SpaceUser> listMyJoinSpace(Long userId){
        // 1.校验参数
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        // 2.根据用户id查询用户加入的团队空间
        return this.baseMapper.selectSpaceUserByUserIdJoin(userId);
    }
}




