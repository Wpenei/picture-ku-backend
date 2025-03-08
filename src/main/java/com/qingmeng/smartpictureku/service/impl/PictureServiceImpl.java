package com.qingmeng.smartpictureku.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.manager.FileManager;
import com.qingmeng.smartpictureku.model.dto.file.UploadPictureResult;
import com.qingmeng.smartpictureku.model.dto.picture.PictureQueryRequest;
import com.qingmeng.smartpictureku.model.dto.picture.PictureUploadRequest;
import com.qingmeng.smartpictureku.model.entity.Picture;
import com.qingmeng.smartpictureku.mapper.PictureMapper;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.vo.PictureVO;
import com.qingmeng.smartpictureku.service.PictureService;
import com.qingmeng.smartpictureku.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Wang
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-03-05 19:39:42
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 校验登录用户
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 判断是新增还是更新图片
        Long pictureId = null;
        if (pictureUploadRequest != null && pictureUploadRequest.getId() != 0) {
            // 更新
            pictureId = pictureUploadRequest.getId();
        }
        // 如果是更新, 查询数据库判断图片是否存在
        if (pictureId != null) {
            boolean exists = this.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR);
        }
        // 上传图片到 COS
        // 根据用户id划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        // 调用图片上传方法
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        // 构建对象存储到数据库中
        boolean save = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "图片上传失败");
        // 获取数据库中的图片信息
//        Picture pictureByKu = this.lambdaQuery().eq(Picture::getUrl, picture.getUrl()).one();
//        return PictureVO.objToVo(pictureByKu);
        return PictureVO.objToVo(picture);
    }

    /**
     * 获取查询条件
     * @param pictureQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        // 从多字段中查询
        if (searchText != null) {
            // 拼接查询条件
            pictureQueryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText));
        }
        pictureQueryWrapper.lambda()
                .eq(ObjUtil.isNotEmpty(id), Picture::getId, id)
                .eq(ObjUtil.isNotEmpty(userId), Picture::getUserId, userId)
                .like(StrUtil.isNotBlank(name), Picture::getName, name)
                .like(StrUtil.isNotBlank(introduction), Picture::getIntroduction, introduction)
                .like(StrUtil.isNotBlank(picFormat), Picture::getPicFormat, picFormat)
                .eq(StrUtil.isNotBlank(category), Picture::getCategory, category)
                .eq(ObjUtil.isNotEmpty(picWidth), Picture::getPicWidth, picWidth)
                .eq(ObjUtil.isNotEmpty(picHeight), Picture::getPicHeight, picHeight)
                .eq(ObjUtil.isNotEmpty(picSize), Picture::getPicSize, picSize)
                .eq(ObjUtil.isNotEmpty(picScale), Picture::getPicScale, picScale);
//        pictureQueryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
//        pictureQueryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
//        pictureQueryWrapper.like(StrUtil.isNotBlank(name), "name", name);
//        pictureQueryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
//        pictureQueryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
//        pictureQueryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
//        pictureQueryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
//        pictureQueryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
//        pictureQueryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
//        pictureQueryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                pictureQueryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        pictureQueryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return pictureQueryWrapper;
    }

    /**
     * 获取图片封装对象Picture VO
     * @param picture
     * @return
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = pictureVO.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                pictureVO.setUserVO(userService.getUserVO(user));
            }
        }
        return pictureVO;
    }

    /**
     * 获取分页查询结果(封装后的)
     * @param picturePage
     * @param request
     * @return
     */
    @Override
    public Page<PictureVO> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVoPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)){
            return pictureVoPage;
        }
        // 将图片对象列表 => 转换成封装对象列表
        List<PictureVO> pictureVoList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 关联查询用户信息
        // 获取用户id集合
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        // 批量查询用户信息
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 填充信息
        pictureVoList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUserVO(userService.getUserVO(user));
        });
        // 将封装对象列表 设置到 分页对象中,并返回
        pictureVoPage.setRecords(pictureVoList);
        return pictureVoPage;
    }

    /**
     * 校验图片
     * @param picture
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

}




