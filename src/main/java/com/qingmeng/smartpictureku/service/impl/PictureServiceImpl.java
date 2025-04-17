package com.qingmeng.smartpictureku.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingmeng.smartpictureku.api.aliyunai.AliYunAiApi;
import com.qingmeng.smartpictureku.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.qingmeng.smartpictureku.api.aliyunai.model.CreateTaskResponse;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.manager.CosManager;
import com.qingmeng.smartpictureku.manager.pictureupload.FilePictureUpload;
import com.qingmeng.smartpictureku.manager.pictureupload.PictureUploadTemplate;
import com.qingmeng.smartpictureku.manager.pictureupload.UrlPictureUpload;
import com.qingmeng.smartpictureku.mapper.PictureMapper;
import com.qingmeng.smartpictureku.model.dto.file.UploadPictureResult;
import com.qingmeng.smartpictureku.model.dto.picture.*;
import com.qingmeng.smartpictureku.model.entity.Picture;
import com.qingmeng.smartpictureku.model.entity.Space;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.enums.PictureReviewStatusEnum;
import com.qingmeng.smartpictureku.model.vo.PictureVO;
import com.qingmeng.smartpictureku.service.PictureService;
import com.qingmeng.smartpictureku.service.SpaceService;
import com.qingmeng.smartpictureku.service.UserService;
import com.qingmeng.smartpictureku.utils.ColorSimilarUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Wang
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-03-05 19:39:42
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {


    @Resource
    private UserService userService;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private SpaceService spaceService;

    @Resource
    private CosManager cosManager;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private Executor pictureKuExecutor;

    @Resource
    private AliYunAiApi aliYunAiApi;

    /**
     * 上传图片
     *
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 校验登录用户
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 添加空间校验
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            // 判断空间是否存在
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "该空间不存在");
            // 判断空间是否属于当前用户
            // 改为统一的权限校验
            //if (!space.getUserId().equals(loginUser.getId())) {
            //    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "该空间不属于当前用户");
            //}
            // 校验空间额度
            if (space.getTotalSize() > space.getMaxSize()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
            }
            // todo 感觉不需要限制条数,只限制空间额度就可以了
            if (space.getTotalCount() > space.getMaxCount()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数不足");
            }
        }
        // 判断是新增还是更新图片
        Long pictureId = null;
        if (pictureUploadRequest != null && pictureUploadRequest.getId() != 0) {
            // 图片有id,说明是更新操作,将id赋值给pictureId
            pictureId = pictureUploadRequest.getId();
        }
        // 如果是更新, 查询数据库判断图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人或管理员可更新
            // 改为统一的权限校验
            //if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            //    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅本人或管理员可更新");
            //}
            // 校验空间是否一致
            // 没传入 spaceId 则使用原图的spaceId
            if (spaceId == null) {
                spaceId = oldPicture.getSpaceId();
            } else {
                // 传了 spaceid 必须和原图一致
                if (!oldPicture.getSpaceId().equals(spaceId)) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间id不一致");
                }
            }

        }
        // 上传图片到 COS
        // 根据 用户id划分目录 => 按照空间id划分目录
        String uploadPathPrefix;
        if (spaceId == null) {
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        } else {
            uploadPathPrefix = String.format("space/%s", spaceId);
        }
        // 根据inputSource类型区分上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        // 调用图片上传方法
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        // 添加缩略图地址
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        String picName = uploadPictureResult.getPicName();
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        picture.setSpaceId(spaceId);
        // todo 如果使用分库分表 必须补充空间id为null(公共空间),默认SpaceId为0
        //if (spaceId == null){
        //    picture.setSpaceId(0L);
        //}
        picture.setPicColor(uploadPictureResult.getPicColor());
        // 补充审核参数
        this.fillReviewParam(picture, loginUser);
        // 如果是更新操作,需要添加图片id和编辑时间
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        // 构建对象存储到数据库中
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            boolean save = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "图片上传失败,数据库操作失败");
            if (finalSpaceId != null) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize + " + uploadPictureResult.getPicSize())
                        .setSql("totalCount = totalCount + 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "空间额度更新失败");
            }
            return picture;
        });
        // todo 如果是更新,可以清理图片资源
        return PictureVO.objToVo(picture);
    }

    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginuser
     */
    @Override
    public void doReviewPicture(PictureReviewRequest pictureReviewRequest, User loginuser) {
        // 1. 参数校验
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        // 不允许将审核过的图片状态修改为待审核
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.判断图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 3. 判断图片状态是否已审核
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请勿重复审核");
        }
        // 4. 更新审核状态
        Picture updatePicture = new Picture();
        updatePicture.setId(id);
        updatePicture.setReviewStatus(reviewStatus);
        updatePicture.setReviewMessage(pictureReviewRequest.getReviewMessage());
        updatePicture.setReviewerId(loginuser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 补充审核参数
     *
     * @param picture
     * @param loginuser
     */
    @Override
    public void fillReviewParam(Picture picture, User loginuser) {
        if (userService.isAdmin(loginuser)) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewMessage("管理员审核自动通过");
            picture.setReviewerId(loginuser.getId());
            picture.setReviewTime(new Date());
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    /**
     * 批量上传图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        // 参数校验
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer searchCount = pictureUploadByBatchRequest.getCount();
        // 构造图片名称前缀
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        ThrowUtils.throwIf(searchCount > 30, ErrorCode.PARAMS_ERROR, "最多上传30张图片");

        // 构造Bing图片搜索地址
        String fetchUrl = StrUtil.format("https://cn.bing.com/images/async?q={}&mmasync=1", URLUtil.encode(searchText));
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        //图片元素
        //Elements imgElementList = div.select("img.mimg");
        // 修改选择器，获取包含完整数据的元素
        Elements imgElementList = div.select(".iusc");
        // 定义成功上传的图片数量
        int successCount = 0;
        for (Element imgElement : imgElementList) {

            // 原方法 - 缩略图
//            String fileUrl = imgElement.attr("src");

            // 获取m属性
            String mAttr = imgElement.attr("m");
            // 将存储有图片信息的JSON数据转为Map集合
            Map<String, String> mMap = JSONUtil.toBean(mAttr, HashMap.class);
            String fileUrl = mMap.get("murl");


            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过: {}", fileUrl);
                continue;
            }
            // 处理图片上传地址，防止出现转义问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            if (StrUtil.isNotBlank(namePrefix)) {
                // 设置图片名称,序号自增
                pictureUploadRequest.setPicName(namePrefix + "-" + (successCount + 1));
            }
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片 {} 上传成功, id = {}", (successCount + 1), pictureVO.getId());
                successCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (successCount >= searchCount) {
                break;
            }
        }
        return successCount;
    }

    /**
     * 删除图片
     *
     * @param pictureId
     * @param loginUser
     */
    @Override
    public void deletePicture(long pictureId, User loginUser) {
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 判断是否存在
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 修改为使用注解鉴权
        // 校验权限
        //checkPictureAuth(oldPicture, loginUser);
        // 开启事务
        transactionTemplate.execute(status -> {
            // 操作数据库
            boolean result = this.removeById(pictureId);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            Long spaceId = oldPicture.getSpaceId();
            if (spaceId != null) {
                // 释放额度
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, spaceId)
                        .setSql(" totalSize  = totalSize  - " + oldPicture.getPicSize())
                        .setSql("totalCount  =totalCount  - 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return true;
        });
        // 异步清理文件
        this.clearPictureFile(oldPicture);
    }

    /**
     * 异步删除COS文件
     *
     * @param oldPicture
     */
    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        // 判断图片是否被多条记录使用
        String pictureUrl = oldPicture.getUrl();
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        long count = this.lambdaQuery().eq(Picture::getUrl, pictureUrl).count();
        if (count > 1) {
            return;
        }
        //
        try {
            // 获取图片路径(去除域名后的)
            String urlKey = new URL(pictureUrl).getPath();
            cosManager.deleteObject(urlKey);
            // 清理缩略图
            if (StrUtil.isNotBlank(thumbnailUrl)) {
                String thumbnailKey = new URL(thumbnailUrl).getPath();
                cosManager.deleteObject(thumbnailKey);
            }
        } catch (MalformedURLException e) {
            log.error("图片Url格式错误", e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "对象存储图片删除失败");
        } catch (Exception e) {
            log.error("对象存储图片删除失败", e);
        }

    }

    /**
     * 图片编辑
     *
     * @param pictureEditRequest
     * @param loginUser
     */
    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        picture.setEditTime(new Date());
        // 校验数据
        this.validPicture(picture);
        // 判断数据是否存在
        long id = pictureEditRequest.getId();

        // todo 如果使用分库分表,必须添加SpaceId
        //QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        //pictureQueryWrapper.eq("id", id);
        //pictureQueryWrapper.eq("spaceId",spaceId);
        //boolean result = pictureService.one(picture, pictureQueryWrapper);
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 权限校验
        // 修改为使用注解鉴权
        //this.checkPictureAuth(oldPicture, loginUser);
        // 补充审核参数
        this.fillReviewParam(picture, loginUser);
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 校验图片
     *
     * @param picture
     * @return
     */
    @Override
    public void checkPictureAuth(Picture picture, User loginUser) {
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            // 公共图库,仅图片本人或管理员可编辑
            if (!userService.isAdmin(loginUser) && !picture.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅图片本人或管理员可编辑");
            }
        } else {
            // 私有图库,仅空间创建人可编辑
            if (!picture.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "私有图片,无权访问");
            }
        }

    }

    /**
     * 获取查询条件
     *
     * @param pictureQueryRequest 图片查询对象
     * @return 图片查询条件
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
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();

        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();

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
                .eq(ObjUtil.isNotEmpty(spaceId), Picture::getSpaceId, spaceId)
                // todo 如果使用分库分表 那么查询公共图库 需要将SpaceId设为0
                //.eq(nullSpaceId,Picture::getSpaceId,0)
                .isNull(nullSpaceId, Picture::getSpaceId)
                .like(StrUtil.isNotBlank(name), Picture::getName, name)
                .like(StrUtil.isNotBlank(introduction), Picture::getIntroduction, introduction)
                .like(StrUtil.isNotBlank(picFormat), Picture::getPicFormat, picFormat)
                .eq(StrUtil.isNotBlank(category), Picture::getCategory, category)
                // todo 修改宽度和高度查询
                .eq(ObjUtil.isNotEmpty(picWidth), Picture::getPicWidth, picWidth)
                .eq(ObjUtil.isNotEmpty(picHeight), Picture::getPicHeight, picHeight)

                .eq(ObjUtil.isNotEmpty(picSize), Picture::getPicSize, picSize)
                .eq(ObjUtil.isNotEmpty(picScale), Picture::getPicScale, picScale)
                .eq(ObjUtil.isNotEmpty(reviewStatus), Picture::getReviewStatus, reviewStatus)
                .like(StrUtil.isNotBlank(reviewMessage), Picture::getReviewMessage, reviewMessage)
                .eq(ObjUtil.isNotEmpty(reviewerId), Picture::getReviewerId, reviewerId)
                // >= startEditTime
                .ge(ObjUtil.isNotEmpty(startEditTime), Picture::getEditTime, startEditTime)
                // <= endEditTime
                .le(ObjUtil.isNotEmpty(endEditTime), Picture::getEditTime, endEditTime);
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
     *
     * @param picture
     * @return
     */
    @Override
    public PictureVO getPictureVO(Picture picture) {
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
     *
     * @param picturePage
     * @param request
     * @return
     */
    @Override
    public Page<PictureVO> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVoPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
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
     *
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

    /**
     * 根据图片主色调查询图片
     *
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    @Override
    public List<PictureVO> getPictureByColor(Long spaceId, String picColor, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 校验空间权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        if (!space.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        }
        // 查询该空间下的所有图片
        List<Picture> pictureList = this.lambdaQuery()
                .eq(Picture::getSpaceId, spaceId)
                //.isNotNull(Picture::getPicColor)
                .list();
        // 如果没有图片直接返回空列表
        if (CollUtil.isEmpty(pictureList)) {
            return Collections.emptyList();
        }
        // 将目标颜色转为Color对象
        Color targetColor = Color.decode(picColor);
        // 计算相似度,并排序
        List<Picture> limitList = pictureList.stream()
                .sorted(Comparator.comparingDouble(picture -> {
                    String hexColor = picture.getPicColor();
                    // 没有主色调的图片放到最后
                    if (StrUtil.isBlank(hexColor)) {
                        return Double.MAX_VALUE;
                    }
                    Color pictureColor = Color.decode(hexColor);
                    // 越大越相似
                    return -ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor);
                }))
                .limit(20)
                .toList();
        // 将查询结果    obj => VO    图片对象列表 => 转换成封装对象列表
        // 这里不是调用Server中的转换方法:  是因为会多查询一次用户信息,没必要
        List<PictureVO> pictureVOList = limitList.stream()
                .map(PictureVO::objToVo)
                .toList();
        return pictureVOList;
    }

    /**
     * 图片批量编辑
     *
     * @param pictureEditByBatchRequest
     * @param loginUser
     */
    @Override
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
        Long spaceId = pictureEditByBatchRequest.getSpaceId();
        String category = pictureEditByBatchRequest.getCategory();
        List<String> tags = pictureEditByBatchRequest.getTags();
        // 1.数据校验
        ThrowUtils.throwIf(spaceId == null || CollUtil.isEmpty(pictureIdList), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 校验空间权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        if (!space.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        }
        // 2.查询优化
        List<Picture> pictureList = this.lambdaQuery()
                .select(Picture::getId, Picture::getSpaceId)
                .eq(Picture::getSpaceId, spaceId)
                .in(Picture::getId, pictureIdList)
                .list();
        if (pictureList.isEmpty()) {
            return;
        }
        // 修改后的批量更新逻辑
        // 每100条分片
        List<List<Picture>> picturePartitions = CollUtil.split(pictureList, 100);
        CountDownLatch latch = new CountDownLatch(picturePartitions.size());

        picturePartitions.forEach(subPictureList ->
                pictureKuExecutor.execute(() ->
                        transactionTemplate.execute(status -> {
                            try {
                                // 1. 设置更新字段（移动到事务内）
                                subPictureList.forEach(pic -> {
                                    if (StrUtil.isNotBlank(category)) {
                                        pic.setCategory(category);
                                    }
                                    if (CollUtil.isNotEmpty(tags)) {
                                        pic.setTags(JSONUtil.toJsonStr(tags));
                                    }
                                });
                                String nameRule = pictureEditByBatchRequest.getNameRule();
                                this.fillPictureWithNameRule(subPictureList, nameRule);
                                // 2. 执行批量更新
                                boolean success = updateBatchById(subPictureList);
                                ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR);
                                return true;
                            } catch (Exception e) {
                                log.error("分片更新失败 batchSize={}", subPictureList.size(), e);
                                status.setRollbackOnly();
                                throw new BusinessException(ErrorCode.OPERATION_ERROR, "分片更新失败");
                            } finally {
                                latch.countDown();
                            }
                        })
                )
        );

        // 3. 等待所有分片完成（带超时）
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "批量更新超时");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "批量更新中断");
        }

    }

    /**
     * 创建图片扩容任务
     * @param createPictureOutPaintingTaskRequest
     * @param loginUser
     * @return
     */
    @Override
    public CreateTaskResponse createOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        // 获取图片信息
        long pictureId = createPictureOutPaintingTaskRequest.getPictureId();
        Picture picture = Optional.ofNullable(this.getById(pictureId))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在"));

        // 权限校验
        // 修改为使用注解鉴权
        //checkPictureAuth(picture,loginUser);
        // 构造请求参数
        CreateOutPaintingTaskRequest taskRequest = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.InputObject inputObject = new CreateOutPaintingTaskRequest.InputObject();
        BeanUtil.copyProperties(createPictureOutPaintingTaskRequest,taskRequest);
        inputObject.setImageUrl(picture.getUrl());
        taskRequest.setInput(inputObject);

        // 创建任务 并返回
        return  aliYunAiApi.createOutPaintingTask(taskRequest);
    }

    /**
     * 根据名称规则填充图片名称
     * nameRule 格式：图片{序号}
     * @param pictureList
     * @param nameRule
     */
    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if (CollUtil.isEmpty(pictureList) || StrUtil.isBlank(nameRule)) {
            return;
        }
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                String pictureName = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
                picture.setName(pictureName);
            }
        } catch (Exception e) {
            log.error("名称解析错误", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "名称解析错误");
        }
    }


}




