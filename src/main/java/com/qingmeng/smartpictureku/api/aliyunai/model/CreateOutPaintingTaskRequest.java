package com.qingmeng.smartpictureku.api.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建图片扩图任务请求对象
 */
@Data
public class CreateOutPaintingTaskRequest implements Serializable {

    private static final long serialVersionUID = -2045001334411477749L;
    /**
     * 模型名称 image-out-painting
     */
    private String model = "image-out-painting";

    /**
     * 图像信息
     */
    private InputObject input;

    /**
     * 图片处理参数
     */
    private Parameters parameters;

    @Data
    public static class InputObject {

        /**
         * 图片地址
         */
        @Alias("image_url")
        private String imageUrl;
    }

    @Data
    public static class Parameters {

        /**
         * 逆时针旋转角度
         */
        private Integer angle;

        /**
         * 图像宽高比
         */
        @Alias("output_ratio")
        private String outputRatio;

        /**
         * 图像居中,在水平方向上按比例扩展图像。
         */
        @Alias("x_scale")
        @JsonProperty("xScale")
        private Float xScale;

        /**
         * 图像居中,垂直方向上按比例扩展图像。
         */
        @Alias("y_scale")
        @JsonProperty("yScale")
        private Float yScale;

        /**
         * 在图像上方添加像素
         */
        @Alias("top_offset")
        private Integer topOffset;

        /**
         * 在图像下方添加像素
         */
        @Alias("bottom_offset")
        private Integer bottomOffset;

        /**
         * 在图像左侧添加像素
         */
        @Alias("left_offset")
        private Integer leftOffset;

        /**
         * 在图像右侧添加像素。
         */
        @Alias("right_offset")
        private Integer rightOffset;

        /**
         * 开启图像最佳质量模式
         */
        @Alias("best_quality")
        private Boolean bestQuality;

        /**
         * 限制模型生成的图像文件大小
         */
        @Alias("limit_image_size")
        private Boolean limitImageSize;

        /**
         * 添加Generated by AI水印
         */
        @Alias("add_watermark")
        private Boolean addWatermark;
    }
}