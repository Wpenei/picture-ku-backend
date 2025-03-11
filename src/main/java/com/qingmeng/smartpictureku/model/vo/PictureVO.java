package com.qingmeng.smartpictureku.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.qingmeng.smartpictureku.model.entity.Picture;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * &#064;description: 图片响应(过滤后)
 *
 * @author Wang
 * &#064;date: 2025/3/5
 */
@Data
public class PictureVO implements Serializable {
    private static final long serialVersionUID = -7261790488013838467L;

    /**
     * id
     */
    private Long id;

    /**
     * 图片 url
     */
    private String url;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组）
     */
    private List<String> tags;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private UserVO userVO;

    /**
     * 缩略图 url
     */
    private String thumbnailUrl;

    /**
     * 空间 id
     */
    private Long spaceId;


    /**
     * 封装类转换对象
     * @param pictureVO
     * @return
     */
    public static Picture voToObj (PictureVO pictureVO){
        if (pictureVO == null){
            return null;
        }
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureVO,picture);
        picture.setTags(pictureVO.getTags().toString());
        return picture;
    }

    /**
     * 封装类转换对象
     * @param picture
     * @return
     */
    public static PictureVO objToVo (Picture picture){
        if (picture == null){
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtil.copyProperties(picture,pictureVO);
        pictureVO.setTags(JSONUtil.toList(picture.getTags(),String.class));
        return pictureVO;
    }
}
