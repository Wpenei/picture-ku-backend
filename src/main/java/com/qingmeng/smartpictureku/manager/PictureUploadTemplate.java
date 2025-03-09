package com.qingmeng.smartpictureku.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qingmeng.smartpictureku.config.CosClientConfig;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * &#064;description: 图片操作类
 *
 * @author Wang
 * &#064;date: 2025/3/5
 */
@Slf4j
public abstract class PictureUploadTemplate {

    // 1 MB
    private static final long ONE_MB = 1024 * 1024;

    // 允许上传的文件后缀
    private static final List<String> ALLOW_UPLOAD_SUFFIX = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param inputSource
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1.校验图片
        checkPicture(inputSource);
        // 2.图片上传地址
        String uuid = RandomUtil.randomString(8);
        // 获取原始文件名
        String originalFilename = getOriginalFilename(inputSource);
        String updateFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("%s/%s", uploadPathPrefix, updateFileName);
        File file = null;
        // 上传图片
        try {
            // 3.创建临时文件
            file = File.createTempFile(uploadPath, null);
            // 创建临时文件(本地或Url
            transferTo(inputSource,file);
            // 上传图片 (根据修改后的路径)
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 获取上传后的图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 封装返回结果 (自定义返回结果)
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double scale = NumberUtil.round( width * 1.0 / height, 2).doubleValue();
            uploadPictureResult.setUrl(cosClientConfig.getHost()+ "/" + uploadPath);
            uploadPictureResult.setPicName(updateFileName);
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(width);
            uploadPictureResult.setPicHeight(height);
            uploadPictureResult.setPicScale(scale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("图片上传到对象存储失败",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片上传失败");
        }finally {
            this.deleteTempFile(file);
        }
        // 封装返回结果
    }

    /**
     * 校验输入源(本地文件或URL）
     * @param inputSource
     */
    protected abstract void checkPicture(Object inputSource);

    /**
     * 获取输入源的原始文件名
     * @param inputSource
     * @return
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 处理输入源并生成本地临时文件
     * @param inputSource
     * @param file
     * @throws IOException
     */
    protected abstract void transferTo(Object inputSource,File file) throws IOException;


    /**
     * 删除临时文件
     * @param file
     */
    private void deleteTempFile(File file) {
        if (file != null) {
            FileUtil.del(file);
        }
        try {
            // 删除临时文件
            file.delete();
        } catch (Exception e) {
            log.error("文件删除失败,文件地址:{}",file.getAbsolutePath(),e);
        }
    }
}
