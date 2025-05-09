package com.qingmeng.smartpictureku.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: 图片修改请求
 *
 * @author Wang
 * &#064;date: 2025/3/5
 */
@Data
public class PictureUploadByBatchRequest implements Serializable {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 抓取数量(默认一次 10 条)
     */
    private Integer count = 10;

    /**
     * 文件名前缀
     */
    private String namePrefix;


    private static final long serialVersionUID = 960147689127316784L;
}
