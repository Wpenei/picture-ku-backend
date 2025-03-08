package com.qingmeng.smartpictureku.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * &#064;description: 图片编辑(普通用户)
 *
 * @author Wang
 * &#064;date: 2025/3/5
 */
@Data
public class PictureEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;
    private static final long serialVersionUID = 960147689127316784L;
}
