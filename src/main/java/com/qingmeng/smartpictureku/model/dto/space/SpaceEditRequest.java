package com.qingmeng.smartpictureku.model.dto.space;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * &#064;description: 空间编辑
 *
 * @author Wang
 * &#064;date: 2025/3/5
 */
@Data
public class SpaceEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;


    private static final long serialVersionUID = 960147689127316784L;
}
