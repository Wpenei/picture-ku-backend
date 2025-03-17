package com.qingmeng.smartpictureku.model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.qingmeng.smartpictureku.model.entity.SpaceUser;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * &#064;description: 空间用户视图
 * @author Wang
 * &#064;date: 2025/3/2
 */
@Data
public class SpaceUserVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 空间用户角色：0-浏览者 1-编辑者 2-管理员
     */
    private Integer spaceRole;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private UserVO userVO;

    private SpaceVO spaceVO;

    private static final long serialVersionUID = 7409930923743653618L;

    /**
     * 封装类转换对象
     * @param spaceUserVO
     * @return
     */
    public static SpaceUser voToObj (SpaceUserVO spaceUserVO){
        if (spaceUserVO == null){
            return null;
        }
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserVO,spaceUser);
        return spaceUser;
    }

    /**
     * 封装类转换对象
     * @param spaceUser
     * @return
     */
    public static SpaceUserVO objToVo (SpaceUser spaceUser){
        if (spaceUser == null){
            return null;
        }
        SpaceUserVO spaceUserVO = new SpaceUserVO();
        BeanUtil.copyProperties(spaceUser,spaceUserVO);
        return spaceUserVO;
    }

}
