package com.qingmeng.smartpictureku.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.qingmeng.smartpictureku.annotation.AuthCheck;
import com.qingmeng.smartpictureku.api.aliyunai.AliYunAiApi;
import com.qingmeng.smartpictureku.api.aliyunai.model.CreateTaskResponse;
import com.qingmeng.smartpictureku.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.qingmeng.smartpictureku.common.BaseResponse;
import com.qingmeng.smartpictureku.common.DeleteRequest;
import com.qingmeng.smartpictureku.common.ResultUtils;
import com.qingmeng.smartpictureku.constant.UserConstant;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.manager.auth.SpaceUserAuthManage;
import com.qingmeng.smartpictureku.manager.auth.StpKit;
import com.qingmeng.smartpictureku.manager.auth.annotation.SaSpaceCheckPermission;
import com.qingmeng.smartpictureku.manager.auth.model.SpaceUserPermissionConstant;
import com.qingmeng.smartpictureku.model.dto.picture.*;
import com.qingmeng.smartpictureku.model.entity.Picture;
import com.qingmeng.smartpictureku.model.entity.Space;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.enums.PictureReviewStatusEnum;
import com.qingmeng.smartpictureku.model.vo.PictureTagCategory;
import com.qingmeng.smartpictureku.model.vo.PictureVO;
import com.qingmeng.smartpictureku.service.PictureService;
import com.qingmeng.smartpictureku.service.SpaceService;
import com.qingmeng.smartpictureku.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * &#064;description:图片接口
 *
 * @author Wang
 * &#064;date: 2025/3/5
 */
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SpaceUserAuthManage spaceUserAuthManage;

    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();
    @Autowired
    private AliYunAiApi aliYunAiApi;


    /**
     * 上传图片(本地文件File)
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过 URL 上传图片 (可重新上传)
     *
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/url")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest,
                                                      HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 批量上传图片
     *
     * @param pictureUploadByBatchRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                                      HttpServletRequest request) {
        // 校验参数
        User loginUser = userService.getLoginUser(request);
        Integer successCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(successCount);
    }

    /**
     * 审核图片
     *
     * @param pictureReviewRequest
     * @param request
     * @return
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> reviewPicture(@RequestBody PictureReviewRequest pictureReviewRequest,
                                               HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doReviewPicture(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 删除图片
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @DeleteMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest,
                                               HttpServletRequest request) {
        // 1.校验请求参数
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.获取请求参数
        User loginUser = userService.getLoginUser(request);
        long pictureId = deleteRequest.getId();
        // todo 如果使用分库分表,必须添加SpaceId
        // 构造 QueryWrapper
        //QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 指定主键 ID
        // 附加 spaceId 条件
        //queryWrapper.eq("id", id)
                //.eq("spaceId", spaceId);
        // 执行删除
        //boolean result = pictureService.remove(queryWrapper);

        pictureService.deletePicture(pictureId, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 编辑图片
     *
     * @param pictureEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(pictureEditRequest == null || pictureEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.editPicture(pictureEditRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片(仅管理员可用)
     *
     * @param pictureUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,
                                               HttpServletRequest request) {
        // 1.校验请求参数
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将DTO转为实体对象
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        // 将List 转为字符串
        picture.setTags(pictureUpdateRequest.getTags().toString());
        // 数据校验
        pictureService.validPicture(picture);
        // 判断数据是否存在
        Long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParam(picture, loginUser);
        // 操作数据库
        // todo 如果使用分库分表,必须添加SpaceId
        //QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        //pictureQueryWrapper.eq("id", id);
        //pictureQueryWrapper.eq("spaceId",spaceId);
        //boolean result = pictureService.update(picture, pictureQueryWrapper);
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取图片(仅管理员可用)
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        // 判断数据是否存在
        //  todo 如果使用分库分表 ,则修改为必须添加SpaceId
        //QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        //queryWrapper.eq("id",id);
        //queryWrapper.eq("spaceId",spaceId);
        //Picture picture = pictureService.getOne(queryWrapper);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(picture);
    }

    // 分页获取图片列表(仅管理员可用)
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        // 校验参数
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 根据id获取脱敏后的图片信息
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVoById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        //  todo 如果使用分库分表 ,则修改为必须添加SpaceId
        //QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        //queryWrapper.eq("id",id);
        //queryWrapper.eq("spaceId",spaceId);
        //Picture picture = pictureService.getOne(queryWrapper);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 空间权限校验
        Long spaceId = picture.getSpaceId();
        Space space = null;
        if (spaceId != null) {
            // 修改为使用编程式鉴权-- 注解式必须登录用户才可以
            boolean b = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!b, ErrorCode.NO_AUTH_ERROR, "权限不足");
            space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        // 获取权限列表
        User loginUser = userService.getLoginUser(request);
        List<String> permissionsList = spaceUserAuthManage.getPermissionsList(space, loginUser);
        PictureVO pictureVO = pictureService.getPictureVO(picture);
        pictureVO.setPermissionList(permissionsList);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 分页获取脱敏后的图片列表
     *
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVoByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        Long spaceId = pictureQueryRequest.getSpaceId();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "查询数据过多");
        if (spaceId == null) {
            // 普通用户只能看到过审的图片
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            // 私有图库,只有空间创建人可以查看
            // 修改为使用编程式鉴权-- 注解式必须登录用户才可以
            boolean b = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!b, ErrorCode.NO_AUTH_ERROR, "没有访问权限");
            //User loginUser = userService.getLoginUser(request);
            //Space space = spaceService.getById(spaceId);
            //ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            //if (!space.getUserId().equals(loginUser.getId())) {
            //    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有该空间访问权限");
            //}
        }
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(pictureService.getPictureVoPage(picturePage, request));
    }

    /**
     * 获取预制标签和分类
     *
     * @return
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "搞怪", "抽象");
        List<String> categoryList = Arrays.asList("头像", "动漫", "壁纸", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }


    /**
     * 从缓存中 分页脱敏后的图片列表
     *
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @Deprecated // 因为无法控制私有空间的更新频率,暂时不使用
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVoByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                      HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "查询数据过多");
        // 普通用户只能看到过审的图片
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());


        // 1.构建缓存key
        // 将查询参数转换为json字符串,作为缓存key
        String queryPicture = JSONUtil.toJsonStr(pictureQueryRequest);
        // md5加密,避免key过长
        String hashKey = DigestUtils.md5DigestAsHex(queryPicture.getBytes());
        String key = String.format("pictureKu:listPictureVoByPage%s", hashKey);

        // 从本地缓存中获取数据
        String cacheValue = LOCAL_CACHE.getIfPresent(key);
        if (cacheValue != null) {
            // 3.如果缓存命中,返回缓存数据
            Page<PictureVO> cachePicture = JSONUtil.toBean(cacheValue, Page.class);
            return ResultUtils.success(cachePicture);
        }
        // 2.从Redis 中获取缓存数据
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        String cacheByRedisValue = opsForValue.get(key);
        if (cacheByRedisValue != null) {
            // 添加到本地缓存中
            LOCAL_CACHE.put(key, cacheByRedisValue);
            // 3.如果缓存命中,返回缓存数据
            Page<PictureVO> cachePicture = JSONUtil.toBean(cacheByRedisValue, Page.class);
            return ResultUtils.success(cachePicture);
        }
        // 4.缓存未命中,查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVoPage = pictureService.getPictureVoPage(picturePage, request);
        String newCacheValue = JSONUtil.toJsonStr(picturePage);

        // 添加到本地缓存中
        LOCAL_CACHE.put(key, newCacheValue);
        // 5.将查询结果存入 redis
        // 过期时间 设置为 5 ~ 10 分钟, 随机数,防止缓存雪崩
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
        // 添加到 Redis 缓存中
        opsForValue.set(key, newCacheValue, cacheExpireTime, TimeUnit.SECONDS);
        return ResultUtils.success(pictureVoPage);
    }

    /**
     * 根据颜色搜索图片
     *
     * @param searchPictureByColorRequest
     * @param request
     * @return
     */
    @PostMapping("/search/color")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest,
                                                              HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        String picColor = searchPictureByColorRequest.getPicColor();
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> pictureByColor = pictureService.getPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(pictureByColor);
    }

    /**
     * 批量修改图片信息
     * @param pictureEditByBatchRequest
     * @param request
     * @return
     */
    @PostMapping("/edit/batch")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 创建AI扩图任务接口
     * @param createPictureOutPaintingTaskRequest ALiYun创建任务请求参数对象
     * @param request 请求
     * @return ALiYun 创建任务响应对象
     */
    @PostMapping("/out_painting/create_task")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<CreateTaskResponse> createOutPaintingTask(
            @RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
            HttpServletRequest request) {
        if (createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        CreateTaskResponse response = pictureService.createOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 查询任务状态接口
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getOutPaintingTask( String taskId){
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
        return ResultUtils.success(task);
    }

}
