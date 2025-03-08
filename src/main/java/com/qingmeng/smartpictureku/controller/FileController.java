package com.qingmeng.smartpictureku.controller;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.qingmeng.smartpictureku.annotation.AuthCheck;
import com.qingmeng.smartpictureku.common.BaseResponse;
import com.qingmeng.smartpictureku.common.ResultUtils;
import com.qingmeng.smartpictureku.constant.UserConstant;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * &#064;description: 文件上传测试类
 *
 * @author Wang
 * &#064;date: 2025/3/5
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private CosManager cosManager;

    /**
     * 测试文件上传
     *
     * @param multipartFile
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        // 获取文件名
        String filename = multipartFile.getOriginalFilename();
        // 设置文件上传路径
        String fileUploadName = String.format("/test/%s", filename);
        File tempFile = null;
        try {
            // 创建临时文件
            tempFile = File.createTempFile(fileUploadName, null);
            multipartFile.transferTo(tempFile);
            cosManager.putObject(fileUploadName, tempFile);
            // 返回访问地址
            return ResultUtils.success(fileUploadName);
        } catch (Exception e) {
            log.info("文件上传失败,文件路径:{}",fileUploadName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"文件上传失败");
        }finally {
            if (tempFile != null){
                // 删除临时文件
                boolean delete = tempFile.delete();
                if (!delete){
                    log.error("文件删除失败,文件路径:{}",fileUploadName);
                }
            }
        }
    }

    /**
     * 测试文件下载
     *
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download/")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            // 根据文件路径获取文件
            COSObject object = cosManager.getObject(filepath);
            // 获取文件输入流
             cosObjectInput = object.getObjectContent();
            // 将文件输入流转换为字节数组
            byte[] byteArray = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();
        }catch (Exception e){
            log.info("文件下载失败,文件路径:{}",filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"文件下载失败");
        }finally {
            cosObjectInput.close();
        }
    }

}
