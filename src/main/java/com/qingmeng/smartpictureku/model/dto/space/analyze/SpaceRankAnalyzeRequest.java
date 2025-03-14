package com.qingmeng.smartpictureku.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * &#064;description:空间分析-大小
 *
 * @author Wang
 * &#064;date: 2025/3/2
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceRankAnalyzeRequest extends SpaceAnalyzeRequest implements Serializable {

    /**
     * 排名前N的空间
     */
    private Integer topN = 10;

    private static final long serialVersionUID = 8324328466280108902L;

}
