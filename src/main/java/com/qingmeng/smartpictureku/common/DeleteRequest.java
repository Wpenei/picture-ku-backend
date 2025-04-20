package com.qingmeng.smartpictureku.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求对象
 * @author Wang
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
