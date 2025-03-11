package com.qingmeng.smartpictureku.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * &#064;description: 图片标签分类响应对象
 *
 * @author Wang
 * &#064;date: 2025/3/6
 */
@Data
@AllArgsConstructor // 全参构造器
public class SpaceLevel implements Serializable {
    private static final long serialVersionUID = 5874836064659143589L;

    private int value;

    private String text;

    private long maxCount;

    private long maxSize;
}
