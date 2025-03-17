package com.qingmeng.smartpictureku.manager.pictureupload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.*;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * &#064;description: Url链接上传图片
 *
 * @author Wang
 * &#064;date: 2025/3/8
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {

    /**
     * 校验输入源(本地文件或URL）
     *
     * @param inputSource
     */
    @Override
    protected void checkPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        ThrowUtils.throwIf(fileUrl == null, ErrorCode.PARAMS_ERROR, "文件Url不能为空");
        // 验证URL格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件Url格式错误");
        }
        // 校验URL协议
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持HTTP 或 HTTPS 协议的文件地址");
        // 发送HEAD请求以验证文件是否存在
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            // 没有正常返回,先不进行其他操作(有可能是不支持HEAD请求)
            if (response.getStatus() != HttpStatus.HTTP_OK){
                return ;
            }
            // 校验文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)){
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 校验文件大小
            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)){
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    // 允许的图片类型
                    long TWO_MB = 1024 * 1024 * 2;
                    ThrowUtils.throwIf(contentLength > TWO_MB, ErrorCode.PARAMS_ERROR, "文件过大,最大支持上传2M");
                }catch (NumberFormatException e){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        }catch (Exception e){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不存在");
        }finally {
            // 关闭资源
            if (response != null){
                response.close();
            }
        }
    }

    /**
     * 获取输入源的原始文件名
     *
     * @param inputSource
     * @return
     */
    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 去掉URL中的查询参数
        int queryIndex = fileUrl.indexOf("?");
        if (queryIndex != -1) {
            fileUrl = fileUrl.substring(0, queryIndex);
        }
        // 从URL中获取名字
        return FileUtil.getName(fileUrl);
        // 这个返回不带后缀的名字
        //return FileUtil.mainName(fileUrl);
    }

    /**
     * 处理输入源并生成本地临时文件
     *
     * @param inputSource
     * @param file
     * @throws IOException
     */
    @Override
    protected void transferTo(Object inputSource, File file) {
        String fileUrl = (String) inputSource;
        // 将文件从URL下载到本地临时文件中
        HttpUtil.downloadFile(fileUrl, file);
    }
}
