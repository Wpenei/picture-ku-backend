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
public class SpaceUserAnalyzeResponse implements Serializable {

    /**
     * 时间区间
     */
    private String period;

    /**
     * 上传数量
     */
    private Long count;



    private static final long serialVersionUID = 7409930923743653618L;

}
