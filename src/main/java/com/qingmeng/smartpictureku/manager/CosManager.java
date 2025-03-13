package com.qingmeng.smartpictureku.manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.qingmeng.smartpictureku.config.CosClientConfig;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * &#064;description: 对象存储操作类
 *
 * @author Wang
 * &#064;date: 2025/3/5
 */
@Component
@Slf4j
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 删除
     *
     * @param key 唯一键
     */
    public void deleteObject(String key) {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }

    /**
     * 上传图片对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        if (file == null) {
            return null;
        }
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        // 对图片进行处理(获取基本信息也是一种处理)
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        // 定义一个集合,用来存放图片处理规则
        List<PicOperations.Rule> ruleList = new ArrayList<>();
        // 将图片转为webp格式(FileUtil.mainName(key) 获取没有后缀的图片名)
        String webpKey = FileUtil.mainName(key) + ".webp";
        // 添加图片压缩规则
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setFileId(webpKey);
        compressRule.setRule("imageMogr2/format/webp");
        compressRule.setBucket(cosClientConfig.getBucket());
        ruleList.add(compressRule);
        // 当原图比较小时, 不进行压缩
        if (file.length() > 2 * 1024) {
            // 缩略图处理
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            // 缩放规则 /thumbnail/<Width>x<Height>> 如果大于原图宽高则不处理
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 256, 256));
            ruleList.add(thumbnailRule);
        }
        // 构造处理函数
        picOperations.setRules(ruleList);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 获取图片主色调
     */
    public String getPictureColor(String key) {
        // 创建获取对象的请求
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        // 设置图片处理规则
        String rule = "imageAve";
        // 获取对象
        COSObject object = cosClient.getObject(getObjectRequest);
        // 读取内容流并解析主色调信息
        try (COSObjectInputStream cosObjectInput = object.getObjectContent();
             ByteArrayOutputStream result = new ByteArrayOutputStream()
        ) {
            // 读取流内容
            byte[] buffer = new byte[1024];
            int num;
            while ((num = cosObjectInput.read(buffer)) != -1) {
                result.write(buffer, 0, num);
            }
            // 将字节数组转换为字符串
            String aveColor = result.toString(StandardCharsets.UTF_8);
            return JSONUtil.parseObj(aveColor).getStr("RGB");
        } catch (Exception e) {
            log.error("获取图片主色调失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口调用失败");
        }
    }

    /**
     * 获取图片主色调（RGB值）
     *
     * @param key 图片存储路径（不含域名）
     * @return RGB值字符串 如"0x2b3149"
     */
    public String getImageMainColor(String key) {
        try {
            // 1.创建请求对象并添加图片处理规则
            GetObjectRequest request = new GetObjectRequest(cosClientConfig.getBucket(), key);
            // 添加数据万象处理参数
            request.putCustomQueryParameter("imageAve", null);

            // 2.执行请求获取处理结果
            COSObject cosObject = cosClient.getObject(request);

            // 3.解析响应数据
            try (COSObjectInputStream inputStream = cosObject.getObjectContent()) {
                // 使用字符流读取器读取COS流,并规定字符编码,同时使用缓读流包装,提高性能,再获取行流,最后合并字符串
                // 读取流内容
                String jsonResult = new BufferedReader(new InputStreamReader(inputStream,StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));

                JSONObject json = JSONUtil.parseObj(jsonResult);
                return json.getStr("RGB");
            }
        } catch (Exception e) {
            log.error("获取图片主色调失败 key={}", key, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片主色调获取失败");
        }
    }

}
