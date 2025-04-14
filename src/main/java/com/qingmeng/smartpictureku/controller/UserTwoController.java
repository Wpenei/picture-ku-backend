/*package com.qingmeng.smartpictureku.controller;

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

import static com.qingmeng.smartpictureku.constant.CommonValue.DEFAULT_PASSWORD;

*//**
 * &#064;description: 用户接口
 *
 * @author Wang
 * &#064;date: 2025/3/1 21:20
 * &#064;version: 1.0
 *//*
//@RestController
//@RequestMapping("/user2")
public class UserTwoController {

    @Resource
    private UserService userService;

    *//**
     * 向邮箱验证码
     *//*
    @PostMapping("/get_emailcode")
    public BaseResponse<String> getEmailCode(@RequestBody EmailCodeRequest emailCodeRequest, HttpServletRequest request) {
        if (emailCodeRequest == null || StrUtil.hasBlank(emailCodeRequest.getEmail(), emailCodeRequest.getType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.sendEmailCode(emailCodeRequest.getEmail(), emailCodeRequest.getType(), request);
        return ResultUtils.success("验证码发送成功");
    }

    *//**
     * 用户注册
     *//*
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 1.校验请求参数
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR, "注册请求参数错误");
        // 2.获取请求参数
        String email = userRegisterRequest.getEmail();
        String code = userRegisterRequest.getCode();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StrUtil.hasBlank(email, code, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册请求参数错误");
        }
        // 3.用户注册
        long result = userService.userRegister(email, code, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    *//**
     * 获取登录验证码
     *//*
    @GetMapping("/getcode")
    public BaseResponse<Map<String, String>> getCode() {
        Map<String, String> captchaData = userService.getCaptcha();
        return ResultUtils.success(captchaData);
    }

    *//**
     * 用户登录
     *//*
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
                                               HttpServletRequest request) {
        // 1.校验请求参数
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        // 2.获取请求参数
        String userAccountOrEmail = userLoginRequest.getUserAccountOrEmail();
        String userPassword = userLoginRequest.getUserPassword();
        String verifyCode = userLoginRequest.getVerifyCode();
        String serververifycode = userLoginRequest.getSerververifycode();
        if (StrUtil.hasBlank(userAccountOrEmail, userPassword, verifyCode, serververifycode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录请求参数错误");
        }
        // 校验验证码
        userService.checkDateCaptcha(verifyCode, serververifycode);
        // 3.用户注册
        LoginUserVO loginUserVO = userService.userLogin(userAccountOrEmail, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    *//**
     * 修改绑定邮箱
     *//*
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

    *//**
     * 获取当前登录用户
     *//*
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }


    *//**
     * 退出登录
     *//*
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    *//**
     * 修改密码
     *//*
    @PostMapping("/changePassword")
    public BaseResponse<Boolean> changePassword(@RequestBody UserModifyPassWordRequest userModifyPassWord,
                                                HttpServletRequest request) {
        ThrowUtils.throwIf(userModifyPassWord == null , ErrorCode.PARAMS_ERROR);
        boolean result = userService.changePassword(userModifyPassWord,request);
        return ResultUtils.success(result);
    }

    *//**
     * 用户注销
     *//*
   *//* @PostMapping("/destroy")
    public BaseResponse<Boolean> userDestroy(@RequestBody DeleteRequest userDestroyRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userDestroyRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 只能注销自己的账号
        ThrowUtils.throwIf(!loginUser.getId().equals(userDestroyRequest.getId()),
                ErrorCode.NO_AUTH_ERROR, "只能注销自己的账号");
        // todo 退出登录状态
        boolean b = userService.userLogout(request);
        if (!b){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "注销失败");
        }
        // 异步删除用户数据
        userService.asyncDeleteUserData(userDestroyRequest.getId());
        return ResultUtils.success(b);
    }*//*

    *//**
     * 更新用户头像
     *//*
    @PostMapping("/update/avatar")
    public BaseResponse<String> updateUserAvatar(MultipartFile multipartFile, Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        String result = userService.updateUserAvatar(multipartFile,id, request);
        return ResultUtils.success(result);
    }

    *//**
     * 创建用户
     *//*
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> userAdd(@RequestBody UserAddRequest userAddRequest) {
        // 1.校验请求参数
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 2.获取请求参数
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        // 设置默认登录密码
        //final String DEFAULT_PASSWORD = "12345678";
        user.setUserPassword(userService.getEncryptPassword(DEFAULT_PASSWORD));
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 3.创建用户
        return ResultUtils.success(result);
    }

    *//**
     * 删除用户
     *//*
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

    *//**
     * 更新用户
     *//*
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

    *//**
     * 根据id获取用户(未脱敏)
     *//*
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

    *//**
     * 根据id获取用户(脱敏)
     *//*
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVoById(Long id) {
        BaseResponse<User> userById = getUserById(id);
        User user = userById.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    *//**
     * 分页获取用户列表(脱敏)
     *//*
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
        Page<UserVO> userVoPage = new Page<>(current, pageSize, userPage.getTotal());
        // 5.将用户信息User转换为脱敏对象UserVO,并返回
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        // 6.将脱敏后的用户信息UserVO添加到分页对象中
        userVoPage.setRecords(userVOList);
        // 7.返回分页对象
        return ResultUtils.success(userVoPage);
    }

    *//**
     * 忘记密码
     *//*
    @PostMapping("/reset/password")
    public BaseResponse<Boolean> resetPassword(@RequestBody UserResetPasswordRequest resetPasswordRequest) {
        if (resetPasswordRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String email = resetPasswordRequest.getEmail();
        String newPassword = resetPasswordRequest.getNewPassword();
        String checkPassword = resetPasswordRequest.getCheckPassword();
        String code = resetPasswordRequest.getCode();

        if (StrUtil.hasBlank(email, newPassword, checkPassword, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean result = userService.resetPassword(email, newPassword, checkPassword, code);
        return ResultUtils.success(result);
    }

    *//**
     * 用户冻结\解封（仅管理员）
     *//*
    @PostMapping("/ban")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> banOrUnbanUser(@RequestBody UserUnbanRequest request, HttpServletRequest httpRequest) {
        if (request == null || request.getUserId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取管理员信息
        User admin = userService.getLoginUser(httpRequest);
        boolean result = userService.banOrUnbanUser(request.getUserId(), request.getIsUnban(), admin);
        return ResultUtils.success(result);
    }
}*/
