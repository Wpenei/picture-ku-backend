package com.qingmeng.smartpictureku.api.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建图片扩图任务请求对象
 */
@Data
public class CreateDoodleTaskRequest implements Serializable {

    private static final long serialVersionUID = -2045001334411477749L;
    /**
     * 模型名称 wanx-sketch-to-image-lite
     */
    private String model = "wanx-sketch-to-image-lite";

    /**
     * 图像信息
     */
    private Input input;

    /**
     * 图片处理参数
     */
    private Parameters parameters;

    @Data
    public static class Input {

        /**
         * 图片地址
         */
        private String prompt;

        /**
         * 图片地址
         */
        @Alias("sketch_image_url")
        private String sketchImageUrl;
    }

    @Data
    public static class Parameters {

        /**
         * 输出图像的风格"
         *<auto>：默认值，由模型随机输出图像风格。
         *
         * <3d cartoon>：3D卡通。
         * <anime>：二次元。
         * <oil painting>：油画。
         * <watercolor>：水彩。
         * <sketch>：素描。
         * <chinese painting>：中国画。
         * <flat illustration>：扁平插画。
         */
        private String style ;

        /**
         * 输出图像的分辨率
         */
        private Boolean size;

        /**
         * 生成图片的数量,默认为4
         */
        private Integer n;

        /**
         * 输入草图对输出图像的约束程度。默认为10 值越大跟草图越相似
         */
        @Alias("sketch_weight")
        private Integer sketchWeight;

        /**
         * 如果上传图片是RGB图片，而非草图（sketch线稿），此参数可控制是否对输入图片进行sketch边缘提取。
         */
        @Alias("sketch_extraction")
        private Boolean sketchExtraction;

        /**
         * 此字段在sketch_extraction=false时生效，所包含数值均被视为画笔色，其余数值均会视为背景色。模型会基于一种或多种画笔色描绘的区域生成新的画作。默认值为[]。
         *
         * 当sketch_image_url线稿中的线条不是黑色，而是包含其他一种或多种颜色时，可以指定一个或多个RGB颜色数值作为画笔色。
         *
         * 示例值：[[134, 134, 134], [0, 0, 0]]
         */
        @Alias("sketch_color")
        private List<List<Integer>> sketchColor;
    }
}