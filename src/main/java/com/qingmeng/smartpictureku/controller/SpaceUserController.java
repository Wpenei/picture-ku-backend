package com.qingmeng.smartpictureku.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.qingmeng.smartpictureku.common.BaseResponse;
import com.qingmeng.smartpictureku.common.DeleteRequest;
import com.qingmeng.smartpictureku.common.ResultUtils;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.manager.auth.annotation.SaSpaceCheckPermission;
import com.qingmeng.smartpictureku.manager.auth.model.SpaceUserPermissionConstant;
import com.qingmeng.smartpictureku.model.dto.spaceuser.SpaceUserAddRequest;
import com.qingmeng.smartpictureku.model.dto.spaceuser.SpaceUserEditRequest;
import com.qingmeng.smartpictureku.model.dto.spaceuser.SpaceUserQueryRequest;
import com.qingmeng.smartpictureku.model.entity.SpaceUser;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.vo.SpaceUserVO;
import com.qingmeng.smartpictureku.service.SpaceUserService;
import com.qingmeng.smartpictureku.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * &#064;description:空间接口
 *
 * @author Wang
 * &#064;date: 2025/3/5
 */
@RestController
@RequestMapping("/spaceUser")
public class SpaceUserController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

    /**
     * 创建空间
     * @param spaceUserAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest,
                                         HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceUserService.addSpaceUser(spaceUserAddRequest, loginUser));
    }

    /**
     * 删除空间中某个成员
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @DeleteMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest,
                                               HttpServletRequest request) {
        // 1.校验请求参数
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.获取请求参数
        long id = deleteRequest.getId();
        // 3.判断空间是否存在
        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 4. 只有空间角色为创建人才可以删除空间
        // 5. 操作数据库
        boolean result = spaceUserService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 编辑成员信息(设置权限
     *
     * @param spaceUserEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceUserEditRequest == null || spaceUserEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserEditRequest, spaceUser);
        // 数据校验
        spaceUserService.validSpaceUser(spaceUser,false);
        // 判断数据是否存在
        long id = spaceUserEditRequest.getId();
        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // todo 空间创建人可编辑
        boolean result = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 查询某个成员在空间中的信息
     *
     * @param spaceUserQueryRequest
     * @return
     */
    @PostMapping("/get")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<SpaceUser> getSpaceUserById(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null , ErrorCode.PARAMS_ERROR);
        Long userId = spaceUserQueryRequest.getUserId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(userId,spaceId), ErrorCode.PARAMS_ERROR);
        // 获取查询条件 并查询
        SpaceUser spaceUser = spaceUserService.getOne(
                spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        // 判断数据是否存在
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUser);
    }

    /**
     * 查询空间成员信息列表
     * @param spaceUserQueryRequest
     * @return
     */
    @PostMapping("/list")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        // 校验参数
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVoList(spaceUserList));
    }

    /**
     * 查询用户加入的团队空间
     * @param request
     * @return
     */
    @PostMapping("/list/my_create")
    public BaseResponse<List<SpaceUserVO>> listMyCreateTeamSpace(HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            return null;
        }
        // 获取用户加入的团队空间
        List<SpaceUser> spaceUserList = spaceUserService.listMyCreateSpace(loginUser.getId());
        // 封装返回结果
        return ResultUtils.success(spaceUserService.getSpaceUserVoList(spaceUserList));
    }
    /**
     * 查询用户加入的团队空间
     * @param request
     * @return
     */
    @PostMapping("/list/my_join")
    public BaseResponse<List<SpaceUserVO>> listMyJoinTeamSpace(HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            return null;
        }
        // 获取用户加入的团队空间
        List<SpaceUser> spaceUserList = spaceUserService.listMyJoinSpace(loginUser.getId());
        // 封装返回结果
        return ResultUtils.success(spaceUserService.getSpaceUserVoList(spaceUserList));
    }
}
