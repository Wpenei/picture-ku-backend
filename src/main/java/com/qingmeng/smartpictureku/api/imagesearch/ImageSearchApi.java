package com.qingmeng.smartpictureku.api.imagesearch;

import com.qingmeng.smartpictureku.api.imagesearch.model.ImageSearchResult;
import com.qingmeng.smartpictureku.api.imagesearch.sub.GetImageFirstUrlApi;
import com.qingmeng.smartpictureku.api.imagesearch.sub.GetImageListApi;
import com.qingmeng.smartpictureku.api.imagesearch.sub.GetImagePageApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064;description:  图片搜索API门面类
 *
 * @author Wang
 * &#064;date: 2025/4/19
 */
@Slf4j
@Component
public class ImageSearchApi {
    /**
     * 搜索图片
     * @param imageUrl
     * @return
     */
    public  List<ImageSearchResult> searchImage(String imageUrl) {
        // 1.根据图片Url搜索，获取搜索的结果Url
        String imagePageUrl = GetImagePageApi.getImagePageApi(imageUrl);
        // 2.根据搜索结果Url获取搜索结果列表
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        // 3.处理结果列表，返回需要的数据
        return GetImageListApi.getImageListApi(imageFirstUrl);
    }

    public static void main(String[] args) {
        ImageSearchApi imageSearchApi = new ImageSearchApi();
        List<ImageSearchResult> imageList = imageSearchApi.searchImage("https://picture-ku-1346826811.cos.ap-shanghai.myqcloud.com/public/1896100181533306881/2025-04-18_jgZZ3DdV_thumbnail.jpg");
        System.out.println("结果列表" + imageList);
    }
}
