package com.qingmeng.smartpictureku.aop;

import com.qingmeng.smartpictureku.annotation.AuthCheck;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.enums.UserRoleEnum;
import com.qingmeng.smartpictureku.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * &#064;description: 权限校验切面类
 * @author Wang
 * &#064;date  2025/3/2
 */
@Aspect
@Component
public class AuthInterceptor {

    private final UserService userService;

    public AuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    /**
     * 执行拦截器
     * @param joinPoint
     * @param authCheck
     * @return
     * @throws Throwable
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 1.获取注解中的角色
        String mustRole = authCheck.mustRole();
        // 将mustRole 转换为枚举类型
        UserRoleEnum enumByValue = UserRoleEnum.getEnumByValue(mustRole);
        // 如果不需要权限,直接放行
        if (enumByValue == null) {
            return joinPoint.proceed();
        }
        // 获取全局 请求上下文对象
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 以下逻辑为判断当前登录用户是否有该权限才能放行
        // 1. 将当前登录用户的角色转换成枚举类型
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 用户没有权限
        ThrowUtils.throwIf(userRoleEnum == null, ErrorCode.NO_AUTH_ERROR);
        // 当前方法需要管理员权限,而当前登录用户没有管理员权限
        if (UserRoleEnum.ADMIN.equals(enumByValue) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"该方法仅管理员可用");
        }
        // 放行
        return joinPoint.proceed();
    }
}
