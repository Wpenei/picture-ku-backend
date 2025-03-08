package com.qingmeng.smartpictureku.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: TODO
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

    private static final long serialVersionUID = 960147689127316784L;
}
