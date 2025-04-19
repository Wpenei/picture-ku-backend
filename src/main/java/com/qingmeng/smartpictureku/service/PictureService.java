package com.qingmeng.smartpictureku.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingmeng.smartpictureku.api.aliyunai.model.CreateTaskResponse;
import com.qingmeng.smartpictureku.model.dto.picture.*;
import com.qingmeng.smartpictureku.model.entity.Picture;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.vo.PictureVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Wang
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-03-05 19:39:42
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 图片审核
     * @param pictureReviewRequest
     * @param loginuser
     */
    void doReviewPicture(PictureReviewRequest pictureReviewRequest,User loginuser);

    /**
     * 补充审核参数
     * @param picture
     * @param loginuser
     */
    void fillReviewParam(Picture picture,User loginuser);

    /**
     * 批量上传图片
     * @param pictureUploadByBatchRequest
     * @param loginuser
     * @return
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest,User loginuser);

    /**
     * 删除图片
     * @param pictureId
     * @param loginUser
     */
    void deletePicture(long pictureId, User loginUser);

    /**
     * 异步删除COS文件
     * @param oldPicture
     */
    void clearPictureFile(Picture oldPicture);

    /**
     * 图片编辑
     * @param pictureEditRequest
     * @param loginUser
     */
    void editPicture(PictureEditRequest pictureEditRequest,User loginUser);

    /**
     * 校验图片
     * @param picture
     * @return
     */
   void checkPictureAuth(Picture picture,User loginUser);











    /**
     * 获取查询条件
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片封装对象Picture VO
     * @param picture
     * @return
     */
    PictureVO getPictureVO(Picture picture,HttpServletRequest request);

    /**
     * 获取图片浏览量
     * @param pictureId 图片id
     * @return 图片浏览量
     */
    long getViewCount(Long pictureId);

    /**
     * 获取分页查询结果(封装后的)
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验图片
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 根据图片主色调查询图片
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    List<PictureVO> getPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 图片批量编辑
     * @param pictureEditByBatchRequest
     * @param loginUser
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest,User loginUser);

    /**
     * 创建图片扩容任务
     * @param createPictureOutPaintingTaskRequest
     * @param loginUser
     * @return
     */
    CreateTaskResponse createOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);

    /**
     * 根据ID获取图片VO
     */
    PictureVO getPictureVOById(Long id, HttpServletRequest request);
}
