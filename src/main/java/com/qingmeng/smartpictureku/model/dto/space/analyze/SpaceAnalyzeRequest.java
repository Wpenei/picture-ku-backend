package com.qingmeng.smartpictureku.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: 空间分析-基础请求对象
 *
 * @author Wang
 * &#064;date: 2025/3/2
 */
@Data
public class SpaceAnalyzeRequest implements Serializable {

    /**
     * 查询全部空间(仅管理员)
     */
    private Boolean queryAll;

    /**
     * 查询公共空间(仅管理员)
     */
    private Boolean queryPublic;

    /**
     * 查询私有空间(仅当 queryAll,queryPublic 都为false时,才添加)
     */
    private Long spaceId;

    private static final long serialVersionUID = 8324328466280108902L;

}
