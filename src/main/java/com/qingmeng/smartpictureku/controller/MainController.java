package com.qingmeng.smartpictureku.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.qingmeng.smartpictureku.common.BaseResponse;
import com.qingmeng.smartpictureku.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * &#064;description: 测试接口

 * @author Wang
 * &#064;date: 2025/2/21 15:33
 * &#064;version: 1.0
 */
@RestController
@RequestMapping("/main")
public class MainController {

    // 会话登录接口
    @PostMapping("doLogin")
    public SaResult doLogin(String name, String pwd) {
        // 第一步：比对前端提交的账号名称、密码
        if("zhang".equals(name) && "123456".equals(pwd)) {
            // 第二步：根据账号id，进行登录
            StpUtil.login(10001);
            StpUtil.getSession().set("name", "张三");
            StpUtil.getSession().set("Age", "23");
            return SaResult.ok("登录成功");
        }
        return SaResult.error("登录失败");
    }

    // 查询登录状态  ---- http://localhost:8081/acc/isLogin
    @GetMapping("isLogin")
    public SaResult isLogin() {
        return SaResult.ok("是否登录：" + StpUtil.isLogin());
    }

    // 查询 Token 信息  ---- http://localhost:8081/acc/tokenInfo
    @GetMapping("tokenInfo")
    public SaResult tokenInfo() {
        return SaResult.data(StpUtil.getTokenInfo());
    }

    // 测试注销  ---- http://localhost:8081/acc/logout
    @GetMapping("logout")
    public SaResult logout() {
        StpUtil.logout();
        return SaResult.ok();
    }
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("He is very health!");
    }
}
