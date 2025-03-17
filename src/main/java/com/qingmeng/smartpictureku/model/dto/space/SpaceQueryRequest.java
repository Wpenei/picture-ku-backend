package com.qingmeng.smartpictureku.model.dto.space;

import com.qingmeng.smartpictureku.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * &#064;description: 空间分页查询请求
 *
 * @author Wang
 * &#064;date: 2025/3/5
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

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

    /**
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 960147689127316784L;
}
