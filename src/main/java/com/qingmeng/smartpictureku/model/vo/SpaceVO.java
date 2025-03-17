package com.qingmeng.smartpictureku.model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.qingmeng.smartpictureku.model.entity.Space;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * &#064;description: 空间视图
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
     * 空间级别：0-私有空间 1-团队空间
     */
    private Integer spaceType;

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
     * 权限列表
     */
    private List<String> permissionList = new ArrayList<>();


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
