package com.qingmeng.smartpictureku.model.dto.picture;

import com.qingmeng.smartpictureku.api.aliyunai.model.CreateOutPaintingTaskRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * &#064;description: 创建图片扩容请求对象
 *
 * @author Wang
 * &#064;date: 2025/3/5
 */
@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {

    /**
     * 扩容图片 id
     */
    private Long pictureId;

    /**
     * 图片扩容请求参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;


    private static final long serialVersionUID = 960147689127316784L;
}
