package com.qingmeng.smartpictureku.model.dto.space;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * &#064;description: 空间更新(给管理员用,可以更新空间限额和空间级别)
 *
 * @author Wang
 * &#064;date: 2025/3/10
 */
@Data
public class SpaceUpdateRequest implements Serializable {
    private static final long serialVersionUID = -8389896197198416692L;
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

}
