package com.qingmeng.smartpictureku.api.imagesearch.sub;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.qingmeng.smartpictureku.api.imagesearch.model.ImageSearchResult;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * &#064;description: step 3 :获取结果列表的api
 *
 * @author Wang
 * &#064;date: 2025/3/12
 */
@Slf4j
public class GetImageListApi {

    public static List<ImageSearchResult> getImageListApi(String url) {
        try {
            // 发送Get请求
            HttpResponse httpResponse = HttpRequest.get(url).execute();
            // 获取响应结果
            int status = httpResponse.getStatus();
            String body = httpResponse.body();
            // 处理响应
            if (status == HttpStatus.HTTP_OK) {
                // 获取结果列表
                return processResponse(body);
            }else {
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"获取图片列表接口调用失败");
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"获取图片列表失败");
        }

    }
    /**
     * 处理接口响应内容
     *
     * @param responseBody 接口返回的JSON字符串
     */
    private static List<ImageSearchResult> processResponse(String responseBody) {
        // 解析响应对象
        JSONObject jsonObject = new JSONObject(responseBody);
        if (!jsonObject.containsKey("data")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (!data.containsKey("list")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }
        JSONArray list = data.getJSONArray("list");
        return JSONUtil.toList(list, ImageSearchResult.class);
    }
    public static void main(String[] args) {
        String imageUrl = "https://graph.baidu.com/ajax/pcsimi?carousel=503&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&inspire=general_pc&limit=30&next=2&render_type=card&session_id=360464546773963649&sign=126d5e97cd54acd88139901745058968&tk=cf846&tpl_from=pc";
        List<ImageSearchResult> imageListApi = getImageListApi(imageUrl);
        System.out.println("搜索结果成功! URL: " + imageListApi);
    }

}