package com.qingmeng.smartpictureku.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingmeng.smartpictureku.model.dto.space.SpaceAddRequest;
import com.qingmeng.smartpictureku.model.dto.space.SpaceQueryRequest;
import com.qingmeng.smartpictureku.model.entity.Space;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author Wang
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-03-10 14:11:50
*/
public interface SpaceService extends IService<Space> {

    /**
     * 创建空间
     * @param spaceAddRequest
     */
    long createSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 获取查询条件
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取空间封装对象SpaceVO
     * @param space
     * @return
     */
    SpaceVO getSpaceVO(Space space);

    /**
     * 获取分页查询结果(封装后的)
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVoPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 校验空间
     * @param space
     */
    void validSpace(Space space, boolean add);

    /**
     * 根据空间级别填充空间限额
     *
     * @param space
     */
    void fileSpaceBySpaceLevel(Space space);


    /**
     * 校验空间权限
     * @param space
     * @param loginUser
     */
    void checkSpaceAuth(Space space, User loginUser);

    SpaceVO getSpaceByUserId(Long userId);
}
