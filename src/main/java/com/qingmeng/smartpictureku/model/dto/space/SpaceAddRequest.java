package com.qingmeng.smartpictureku.model.dto.space;

import lombok.Getter;

import java.io.Serializable;

/**
 * &#064;description: 创建空间请求
 *
 * @author Wang
 * &#064;date: 2025/3/2
 */
@Getter
public class SpaceAddRequest implements Serializable {

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间级别：0-私有空间 1-团队空间
     */
    private Integer spaceType;


    private static final long serialVersionUID = 724272877946501103L;
}
