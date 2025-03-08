package com.qingmeng.smartpictureku.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * &#064;description: TODO
 *
 * @author Wang
 * &#064;date: 2025/3/6
 */
@Data
public class PictureTagCategory implements Serializable {
    private static final long serialVersionUID = 5874836064659143589L;

    private List<String> tagList;

    private List<String> categoryList;
}
