package com.qingmeng.smartpictureku.api.imagesearch.model;

import lombok.Data;

/**
 * &#064;description: todo 以图搜图返回结果(还未实现)
 *
 * @author Wang
 * &#064;date: 2025/3/12
 */
@Data
public class ImageSearchResult {

    /**
     * 图片地址
     */
    private String thumbUrl;

    /**
     * 图片来源
     */
    private String fromUrl;
}
