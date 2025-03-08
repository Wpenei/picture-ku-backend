package com.qingmeng.smartpictureku.controller;

import com.qingmeng.smartpictureku.common.BaseResponse;
import com.qingmeng.smartpictureku.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * &#064;description: 测试接口

 * @author Wang
 * &#064;date: 2025/2/21 15:33
 * &#064;version: 1.0
 */
@RestController
@RequestMapping("/")
public class MainController {
    @GetMapping("/health")
    public BaseResponse<String> index() {
        return ResultUtils.success("He is very health!");
    }
}
