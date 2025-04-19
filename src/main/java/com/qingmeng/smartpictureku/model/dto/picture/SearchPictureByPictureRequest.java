package com.qingmeng.smartpictureku.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: 图片编辑(普通用户)
 *
 * @author Wang
 * &#064;date: 2025/3/5
 */
@Data
public class SearchPictureByPictureRequest implements Serializable {

    /**
     * id
     */
    private Long id;
    private static final long serialVersionUID = 960147689127316784L;
}
