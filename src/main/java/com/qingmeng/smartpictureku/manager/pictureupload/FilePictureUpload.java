package com.qingmeng.smartpictureku.manager.pictureupload;

import cn.hutool.core.io.FileUtil;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * &#064;description: 本地文件上传图片
 *
 * @author Wang
 * &#064;date: 2025/3/8
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    // 1 M
    private static final long ONE_MB = 1024 * 1024;

    // 允许上传的文件后缀
    private static final List<String> ALLOW_UPLOAD_SUFFIX = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    /**
     * 校验输入源(本地文件或URL）
     * @param inputSource
     */
    @Override
    protected void checkPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        // 判断文件是否为空
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 1.校验文件大小
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > ONE_MB * 2, ErrorCode.PARAMS_ERROR, "文件大小不能超过2M");
        // 2.校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOW_UPLOAD_SUFFIX.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "不支持的文件类型");

    }

    /**
     * 获取输入源的原始文件名
     * @param inputSource
     * @return
     */
    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    /**
     * 处理输入源并生成本地临时文件
     * @param inputSource
     * @param file
     * @throws IOException
     */
    @Override
    protected void transferTo(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        //将上传的 MultipartFile 文件内容写入到指定的本地临时文件中
        multipartFile.transferTo(file);
    }
}
