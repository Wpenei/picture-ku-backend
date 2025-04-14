package com.qingmeng.smartpictureku.constant;

/**
* &#064;description: 用户常量
* @author Wang
* &#064;date: 2025/3/2 15:03
*/
public interface EmailConstant {
    /**
     * 邮箱验证码
     */
    String EMAIL_CODE_VERITY = "email:code:verity";
    /**
     * 过期时间
     */
    Integer EMAIL_CODE_EXPIRE_TIME= 5;

    //  region 邮件类型

    /**
     * 注册
     */
    String REGISTER = "register";

    /**
     * 重置密码
     */
    String RESET_PASSWORD = "resetPassword";

    /**
     * 修改邮箱
     */
    String CHANGE_EMAIL = "changeEmail";

    /**
     * 警告
     */
    String WARNING = "warning";

    /**
     * 封禁
     */
    String BAN = "ban";

    // endregion
}
