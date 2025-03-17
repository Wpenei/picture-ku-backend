package com.qingmeng.smartpictureku.manager.auth.model;

/**
* &#064;description: 空间成员权限常量
* @author Wang
* &#064;date: 2025/3/2 15:03
*/
public interface SpaceUserPermissionConstant {

    /**
     * 空间成员管理
     */
    String SPACE_USER_MANAGE = "spaceUser:manage";

    //  region 图片相关权限

    /**
     * 查看图片
     */
    String PICTURE_VIEW = "picture:view";

    /**
     * 上传图片
     */
    String PICTURE_UPLOAD = "picture:upload";

    /**
     * 编辑图片
     */
    String PICTURE_EDIT = "picture:edit";

    /**
     * 删除图片
     */
    String PICTURE_DELETE = "picture:delete";

    // endregion
}
