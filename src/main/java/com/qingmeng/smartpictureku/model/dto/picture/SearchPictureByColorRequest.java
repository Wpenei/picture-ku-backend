package com.qingmeng.smartpictureku.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: 根据主色调查询图片请求
 *
 * @author Wang
 * &#064;date: 2025/3/5
 */
@Data
public class SearchPictureByColorRequest  implements Serializable {

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 图片主色调
     */
    private String picColor;

    private static final long serialVersionUID = 960147689127316784L;
}
