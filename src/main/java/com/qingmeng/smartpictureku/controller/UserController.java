package com.qingmeng.smartpictureku.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingmeng.smartpictureku.annotation.AuthCheck;
import com.qingmeng.smartpictureku.common.BaseResponse;
import com.qingmeng.smartpictureku.common.DeleteRequest;
import com.qingmeng.smartpictureku.common.ResultUtils;
import com.qingmeng.smartpictureku.constant.UserConstant;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.model.dto.user.*;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.vo.LoginUserVO;
import com.qingmeng.smartpictureku.model.vo.UserVO;
import com.qingmeng.smartpictureku.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * &#064;description: 用户接口
 *
 * @author Wang
 * &#064;date: 2025/3/1 21:20
 * &#064;version: 1.0
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 邮箱注册验证码
     * @param emailCodeRequest 邮箱验证码请求
     * @param request 请求
     * @return 验证码发送结果
     */
    @PostMapping("/get/emailCode")
    public BaseResponse<String> getEmailCode(@RequestBody EmailCodeRequest emailCodeRequest, HttpServletRequest request) {
        // 1.校验请求参数
        ThrowUtils.throwIf(emailCodeRequest == null, ErrorCode.PARAMS_ERROR);
        // 2.获取请求参数
        String email = emailCodeRequest.getEmail();
        String type = emailCodeRequest.getType();
        // 3.发送验证码 异步获取
        userService.sendEmailCode(email, type, request);
        return ResultUtils.success("验证码发送成功。");
    }

    /**
     * 用户注册
     * @param userRegisterRequest 用户注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 1.校验请求参数
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        // 2.获取请求参数
        String email = userRegisterRequest.getEmail();
        String code = userRegisterRequest.getCode();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 3.用户注册
        long result = userService.userRegister(email,code, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 获取登录验证码
     * @return 登录验证码
     */
    @GetMapping("/get/captcha")
    public BaseResponse<Map<String,String>> getLoginCaptcha() {
        Map<String,String> loginCaptcha =  userService.getLoginCaptcha();
        return ResultUtils.success(loginCaptcha);
    }

    /**
     * 用户登录
     * @param userLoginRequest 用户登录请求
     * @param request 请求
     * @return 登录用户信息-脱敏
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
                                               HttpServletRequest request) {
        // 1.校验请求参数
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        // 2.获取请求参数
        String email = userLoginRequest.getEmail();
        String userPassword = userLoginRequest.getUserPassword();
        String inputVerifyCode = userLoginRequest.getInputVerifyCode();
        String serverVerifyCode = userLoginRequest.getServerVerifyCode();
        // 校验验证码
        boolean b = userService.checkCaptcha(inputVerifyCode, serverVerifyCode);
        ThrowUtils.throwIf(!b, ErrorCode.PARAMS_ERROR,"验证码错误");
        // 3.用户登录
        LoginUserVO loginUserVO = userService.userLogin(email, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 更新用户头像
     * @param multipartFile 头像文件
     * @param id 用户id
     * @param request 请求
     * @return 更新结果
     */
    @PostMapping("/update/avatar")
    public BaseResponse<String> updateUserAvatar(MultipartFile multipartFile, Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        String result = userService.updateUserAvatar(multipartFile,id, request);
        return ResultUtils.success(result);
    }

    /**
     * 修改密码
     * @param userModifyPassWord 修改密码请求
     * @param request 请求
     * @return 修改结果
     */
    @PostMapping("/changePassword")
    public BaseResponse<Boolean> changePassword(@RequestBody UserModifyPassWordRequest userModifyPassWord, HttpServletRequest request) {
        ThrowUtils.throwIf(userModifyPassWord == null, ErrorCode.PARAMS_ERROR);
        Long id = userModifyPassWord.getId();
        String oldPassword = userModifyPassWord.getOldPassword();
        String newPassword = userModifyPassWord.getNewPassword();
        String checkPassword = userModifyPassWord.getCheckPassword();
        boolean result = userService.changePassword(id,oldPassword,newPassword,checkPassword, request);
        return ResultUtils.success(result);
    }

    /**
     * 修改绑定邮箱
     * @param userChangeEmailRequest 修改绑定邮箱请求
     * @param request 请求
     * @return 修改结果
     */
    @PostMapping("/change/email")
    public BaseResponse<Boolean> changeEmail(@RequestBody UserChangeEmailRequest userChangeEmailRequest, HttpServletRequest request) {
        if (userChangeEmailRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String newEmail = userChangeEmailRequest.getNewEmail();
        String code = userChangeEmailRequest.getCode();
        if (StrUtil.hasBlank(newEmail, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.changeEmail(newEmail, code, request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     * @param request 请求
     * @return 当前登录用户-脱敏
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        // 1.获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 2.数据脱敏并返回
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 退出登录
     * @param request 请求
     * @return 退出结果
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 重置密码
     * @param resetPasswordRequest 重置密码请求
     * @return 重置结果
     */
    @PostMapping("/reset/password")
    public BaseResponse<Boolean> resetPassword(@RequestBody UserResetPasswordRequest resetPasswordRequest) {
        ThrowUtils.throwIf(resetPasswordRequest == null,ErrorCode.PARAMS_ERROR);
        String email = resetPasswordRequest.getEmail();
        String newPassword = resetPasswordRequest.getNewPassword();
        String checkPassword = resetPasswordRequest.getCheckPassword();
        String code = resetPasswordRequest.getCode();
        boolean result = userService.resetPassword(email, newPassword, checkPassword, code);
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取用户
     * @param id 用户id
     * @return 用户信息-脱敏
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVoById(Long id) {
        BaseResponse<User> userById = getUserById(id);
        User user = userById.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 用户注销
     * @param userDestroyRequest 用户注销请求
     * @param request 请求
     * @return 注销结果
     */
    @PostMapping("/destroy")
    public BaseResponse<Boolean> userDestroy(@RequestBody DeleteRequest userDestroyRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userDestroyRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 只能注销自己的账号
        ThrowUtils.throwIf(!loginUser.getId().equals(userDestroyRequest.getId()),
                ErrorCode.NO_AUTH_ERROR, "只能注销自己的账号");
        // 异步删除用户数据
        userService.asyncDeleteUserData(userDestroyRequest.getId(),request);
        return ResultUtils.success(true);
    }

// region 管理员

    /**
     * 根据id获取用户
     *
     * @param id 用户id
     * @return 用户信息-未脱敏
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(user);
    }

    /**
     * 删除用户
     * @param deleteRequest 删除请求
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        // 1.校验请求参数
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 2.获取请求参数
        long id = deleteRequest.getId();
        // 3.删除用户
        boolean result = userService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }

    /**
     * 创建用户
     * @param userAddRequest 创建用户请求
     * @return 创建结果
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> userAdd(@RequestBody UserAddRequest userAddRequest) {
        // 1.校验请求参数
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 2.获取请求参数
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        // 设置默认登录密码
        final String DEFAULT_PASSWORD = "12345678";
        user.setUserPassword(userService.getEncryptPassword(DEFAULT_PASSWORD));
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 3.创建用户
        return ResultUtils.success(result);
    }

    /**
     * 更新用户
     * @param userUpdateRequest 更新用户请求
     * @return 更新结果
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {

        // 1.校验请求参数
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 2.将请求参数拷贝到实体类中
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        // 3.更新用户
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }


    /**
     * 分页获取用户列表
     * @param userQueryRequest 用户分页查询请求
     * @return 用户列表-分页、脱敏
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> getUserList(@RequestBody UserQueryRequest userQueryRequest) {
        // 1.校验请求参数
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 2.获取分页参数
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        // 3.分页查询, 根据请求参数构造查询条件
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        // 4.构造返回结果
        Page<UserVO> userVoPage = new Page<>(current,pageSize, userPage.getTotal());
        // 5.将用户信息User转换为脱敏对象UserVO,并返回
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        // 6.将脱敏后的用户信息UserVO添加到分页对象中
        userVoPage.setRecords(userVOList);
        // 7.返回分页对象
        return ResultUtils.success(userVoPage);
    }

    /**
     * 用户封禁/解禁
     * @param userUnbanRequest 封禁/解禁请求
     * @param request 请求
     * @return 封禁结果
     */
    @PostMapping("/ban")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> banOrUnbanUser(@RequestBody UserUnbanRequest userUnbanRequest, HttpServletRequest request) {
        if (userUnbanRequest == null || userUnbanRequest.getUserId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = userUnbanRequest.getUserId();
        Boolean isUnban = userUnbanRequest.getIsUnban();
        // 获取管理员信息
        User admin = userService.getLoginUser(request);

        boolean result = userService.banOrUnbanUser(userId, isUnban, admin);
        return ResultUtils.success(result);
    }

// endregion
}
