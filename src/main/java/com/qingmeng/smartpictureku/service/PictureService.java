package com.qingmeng.smartpictureku.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingmeng.smartpictureku.model.dto.picture.PictureQueryRequest;
import com.qingmeng.smartpictureku.model.dto.picture.PictureReviewRequest;
import com.qingmeng.smartpictureku.model.dto.picture.PictureUploadByBatchRequest;
import com.qingmeng.smartpictureku.model.dto.picture.PictureUploadRequest;
import com.qingmeng.smartpictureku.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

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
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

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
     * 删除文件
     * @param oldPicture
     */
    void deletePicture(Picture oldPicture);
}
