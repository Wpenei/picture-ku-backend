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
public class PictureUploadRequest implements Serializable {

    /**
     * 图片id(仅用于修改)
     */
    private long id;


    /**
     * 图片URL
     */
    private String fileUrl;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 空间 id
     */
    private Long spaceId;


    private static final long serialVersionUID = 960147689127316784L;
}
