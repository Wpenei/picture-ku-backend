package com.qingmeng.smartpictureku.model.dto.picture;

import com.qingmeng.smartpictureku.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * &#064;description: 图片分页查询请求
 *
 * @author Wang
 * &#064;date: 2025/3/5
 */
@Data
public class PictureReviewRequest implements Serializable {

    private static final long serialVersionUID = 1173399724325543669L;

    /**
     * id
     */
    private Long id;

    /**
     * 审核状态：0-审核中; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;


}
