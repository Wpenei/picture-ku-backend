package com.qingmeng.smartpictureku.model.vo.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * &#064;description: 用户视图
 * @author Wang
 * &#064;date: 2025/3/2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceSizeAnalyzeResponse implements Serializable {

    /**
     * 图片大小范围
     */
    private String sizeRange;

    /**
     * 图片数量
     */
    private Long count;



    private static final long serialVersionUID = 7409930923743653618L;

}
