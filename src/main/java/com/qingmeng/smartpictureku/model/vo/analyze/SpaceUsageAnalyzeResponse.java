package com.qingmeng.smartpictureku.model.vo.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * &#064;description: 用户试图
 * @author Wang
 * &#064;date: 2025/3/2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceUsageAnalyzeResponse implements Serializable {

    /**
     * 已使用大小
     */
    private Long userSize;
    /**
     * 总大小
     */
    private Long maxSize;

    /**
     * 已使用大小比例
     */
    private Double sizeUsageRadio;

    /**
     * 已有数量
     */
    private Long userCount;
    /**
     * 最大数量
     */
    private Long maxCount;

    /**
     * 已使用数量比例
     */
    private Double countUsageRadio;



    private static final long serialVersionUID = 7409930923743653618L;

}
