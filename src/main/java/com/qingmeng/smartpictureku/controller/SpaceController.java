package com.qingmeng.smartpictureku.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingmeng.smartpictureku.annotation.AuthCheck;
import com.qingmeng.smartpictureku.common.BaseResponse;
import com.qingmeng.smartpictureku.common.DeleteRequest;
import com.qingmeng.smartpictureku.common.ResultUtils;
import com.qingmeng.smartpictureku.constant.UserConstant;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.model.dto.space.*;
import com.qingmeng.smartpictureku.model.entity.Space;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.enums.SpaceLevelEnum;
import com.qingmeng.smartpictureku.model.vo.SpaceLevel;
import com.qingmeng.smartpictureku.model.vo.SpaceVO;
import com.qingmeng.smartpictureku.service.SpaceService;
import com.qingmeng.smartpictureku.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * &#064;description:空间接口
 *
 * @author Wang
 * &#064;date: 2025/3/5
 */
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    /**
     * 创建空间
     * @param spaceAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest,
                                         HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long newSpaceId = spaceService.createSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(newSpaceId);
    }

    /**
     * 删除空间
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest,
                                               HttpServletRequest request) {
        // 1.校验请求参数
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.获取请求参数
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 3.判断空间是否存在
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 4. 判断权限
        if (!userService.isAdmin(loginUser) && !space.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 5. 操作数据库
        boolean result = spaceService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 编辑空间
     *
     * @param spaceEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceEditRequest == null || spaceEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Space space = new Space();
        BeanUtil.copyProperties(spaceEditRequest, space);
        space.setEditTime(new Date());
        User loginUser = userService.getLoginUser(request);
        // 判断数据是否存在
        long id = spaceEditRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅空间创建人可编辑
        if (!oldSpace.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "不能编辑别人的空间");
        }
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新空间(仅管理员可用,允许更新空间级别)
     *
     * @param spaceUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
        // 1.校验请求参数
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将DTO转为实体对象
        Space space = new Space();
        BeanUtil.copyProperties(spaceUpdateRequest, space);
        // 填充限额
        spaceService.fileSpaceBySpaceLevel(space);
        // 数据校验
        spaceService.validSpace(space,false);
        // 判断数据是否存在
        Long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);

        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取空间(仅管理员可用)
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        // 判断数据是否存在
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(space);
    }

    // 分页获取空间列表(仅管理员可用)
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        // 校验参数
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        Page<Space> spacePage = spaceService.page(new Page<>(current, pageSize),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }

    /**
     * 根据id获取脱敏后的空间信息
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVoById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(id);
        return ResultUtils.success(spaceService.getSpaceVO(space, request));
    }

    /**
     * 分页获取脱敏后的空间列表
     *
     * @param spaceQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVoByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
                                                             HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "查询数据过多");
        Page<Space> spacePage = spaceService.page(new Page<>(current, pageSize),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spaceService.getSpaceVoPage(spacePage, request));
    }

    /**
     * 获取空间级别信息
     * @return
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> getSpaceLevel(){
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize())).toList();
        return ResultUtils.success(spaceLevelList);
    }

}
