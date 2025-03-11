package com.qingmeng.smartpictureku.model.dto.space;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;

import java.io.Serializable;
import java.util.Date;

/**
 * &#064;description: 创建空间请求
 *
 * @author Wang
 * &#064;date: 2025/3/2
 */
@Getter
public class SpaceAddRequest implements Serializable {

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;


    private static final long serialVersionUID = 724272877946501103L;
}
