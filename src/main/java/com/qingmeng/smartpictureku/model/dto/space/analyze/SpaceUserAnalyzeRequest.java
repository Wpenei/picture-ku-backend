package com.qingmeng.smartpictureku.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * &#064;description: 空间分析-用户上传行为
 *
 * @author Wang
 * &#064;date: 2025/3/2
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest implements Serializable {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 时间维度
     */
    private String timeDimension;

    private static final long serialVersionUID = 8324328466280108902L;

}
