package com.qingmeng.smartpictureku.api.imagesearch.sub;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * &#064;description: step 1 :获取以图搜图的结果地址api
 *
 * @author Wang
 * &#064;date: 2025/3/12
 */
@Slf4j
public class GetImagePageApi {

    public static String getImagePageApi(String imageUrl) {
        // 1. 准备请求参数
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 获取当前时间戳
        long uptime = System.currentTimeMillis();
        // 请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;

        // 2. 发送POST请求到百度接口
        try {
            HttpResponse httpResponse = HttpRequest.post(url)
                    .header("acs-token", RandomUtil.randomString(6))
                    .form(formData)
                    .timeout(5000)
                    .execute();
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口调用失败");
            }
            // 解析响应
            String responseBody = httpResponse.body();
            Map<String ,Object> result = JSONUtil.toBean(responseBody, Map.class);

            // 3.处理响应结果
            if (result == null || !Integer.valueOf(0).equals(result.get("status"))){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "处理响应结果时,接口调用失败");
            }
            Map<String ,Object> data =(Map<String, Object>) result.get("data");
            // 对URL进行解析
            String rawUrl = (String) data.get("url");
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            if (StrUtil.isBlank(searchResultUrl)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "未返回有效结果地址");
            }
            return searchResultUrl;

        }catch (Exception e){
            log.error("调用百度以图搜图接口失败",e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "搜索失败");
        }
    }

    public static void main(String[] args) {
        String imageUrl = "https%3A%2F%2Fwww.codefather.cn%2Flogo.png";
        String imagePageApi = getImagePageApi(imageUrl);
        System.out.println("搜索结果成功! URL: " + imagePageApi);
    }

}