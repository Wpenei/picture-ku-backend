package com.qingmeng.smartpictureku.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * &#064;description: 空间分析-资源使用
 *
 * @author Wang
 * &#064;date: 2025/3/2
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUsageAnalyzeRequest extends SpaceAnalyzeRequest implements Serializable {


    private static final long serialVersionUID = 8324328466280108902L;

}
