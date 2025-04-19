package com.qingmeng.smartpictureku.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingmeng.smartpictureku.model.dto.user.UserQueryRequest;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.vo.LoginUserVO;
import com.qingmeng.smartpictureku.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * &#064;description: 用户接口
 *
 * @author Wang
 * &#064;date: 2025-03-01 20:09:42
 */
public interface UserService extends IService<User> {

    /**
     * 发送邮箱验证码
     */
    void sendEmailCode(String email, String type, HttpServletRequest request);

    /**
     * 用户注册
     * @param email 邮箱
     * @param code 验证码
     * @param userPassword 密码
     * @param checkPassword 确认密码
     * @return 账户id
     */
    long userRegister(String email,String code , String userPassword, String checkPassword);

    /**
     * 获取登录验证码
     */
    Map<String, String> getLoginCaptcha();

    /**
     * 校验用户输入验证码
     */
    boolean checkCaptcha(String userInputCaptcha, String serverVerifyCode);
    /**
     * 用户登录
     */
    LoginUserVO userLogin(String email, String userPassword, HttpServletRequest request);

    /**
     * 修改密码
     */
    boolean changePassword(Long id, String oldPassword, String newPassword, String checkPassword, HttpServletRequest request);


    /**
     * 获取当前登录用户
     * @param request 请求
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 退出登录
     * @param request 请求
     * @return 退出结果
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏后的登录用户
     * @param user 用户
     * @return 登录用户-脱敏
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取用户信息(脱敏)
     * @param user 用户
     * @return 用户-脱敏
     */
    UserVO getUserVO(User user);

    /**
     * 获取用户信息列表(脱敏)
     * @param userList 用户列表
     * @return 用户列表-脱敏
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取查询条件
     * @param userQueryRequest 查询条件对象
     * @return 查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 获取加密密码
     * @param userPassword 密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 判断用户是否为管理员
     */
    boolean isAdmin(User user);

    /**
     * 更新用户头像
     * @param multipartFile 头像文件
     * @param id 用户id
     * @param request 请求
     * @return 更新结果
     */
    String updateUserAvatar(MultipartFile multipartFile, Long id, HttpServletRequest request);

    /**
     * 更换绑定邮箱
     * @param newEmail 新邮箱
     * @param code 邮箱验证码
     * @param request 请求
     * @return 更换结果
     */
    boolean changeEmail(String newEmail, String code, HttpServletRequest request);

    /**
     * 重置密码
     * @param email 邮箱
     * @param newPassword 新密码
     * @param checkPassword 确认密码
     * @param code 验证码
     * @return 重置结果
     */
    boolean resetPassword(String email, String newPassword, String checkPassword, String code);

    /**
     * 封禁/解禁用户
     * @param userId 目标用户id
     * @param isUnban true-解禁，false-封禁
     * @param admin 执行操作的管理员
     * @return 是否操作成功
     */
    boolean banOrUnbanUser(Long userId, Boolean isUnban, User admin);

    /**
     * 用户注销
     * @param userId 用户id
     */
    void asyncDeleteUserData(Long userId, HttpServletRequest request);

    /**
     * 判断是否是登录态
     */
    User isLogin(HttpServletRequest request);
}
