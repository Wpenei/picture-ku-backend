package com.qingmeng.smartpictureku.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.qingmeng.smartpictureku.model.entity.Space;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * &#064;description: 用户试图
 * @author Wang
 * &#064;date: 2025/3/2
 */
@Data
public class SpaceVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    /**
     * 当前空间下图片的总大小
     */
    private Long totalSize;

    /**
     * 当前空间下的图片数量
     */
    private Long totalCount;

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

    private static final long serialVersionUID = 7409930923743653618L;

    /**
     * 封装类转换对象
     * @param spaceVO
     * @return
     */
    public static Space voToObj (SpaceVO spaceVO){
        if (spaceVO == null){
            return null;
        }
        Space space = new Space();
        BeanUtil.copyProperties(spaceVO,space);
        return space;
    }

    /**
     * 封装类转换对象
     * @param space
     * @return
     */
    public static SpaceVO objToVo (Space space){
        if (space == null){
            return null;
        }
        SpaceVO spaceVO = new SpaceVO();
        BeanUtil.copyProperties(space,spaceVO);
        return spaceVO;
    }

}
