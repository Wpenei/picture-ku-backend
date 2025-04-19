package com.qingmeng.smartpictureku.controller;

import com.qingmeng.smartpictureku.common.BaseResponse;
import com.qingmeng.smartpictureku.common.ResultUtils;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.model.dto.message.MessageAddRequest;
import com.qingmeng.smartpictureku.model.vo.MessageVO;
import com.qingmeng.smartpictureku.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * &#064;description: 留言板
 *
 * @author Wang
 * &#064;date: 2025/4/19
 */
@RestController
@RequestMapping("/message")
@Slf4j
public class MessageController {

    @Resource
    private MessageService messageService;



    /**
     * 添加留言
     * @param request 请求
     * @return 未读消息总数
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addMessage(@RequestBody MessageAddRequest messageAddRequest,
                                                          HttpServletRequest request){
        ThrowUtils.throwIf(messageAddRequest == null,ErrorCode.PARAMS_ERROR);
        // 根据请求获取IP地址
        String ip = getIpAddress(request);
        messageAddRequest.setIp(ip);
        return ResultUtils.success(messageService.addMessage(messageAddRequest));
    }



    /**
     * 获取消息中心未读消息
     * @return 未读消息总数
     */
    @GetMapping("/getTop500")
    public BaseResponse<List<MessageVO>> getMessageTop500(){
        return ResultUtils.success(messageService.getTop500());
    }

    /**
     * 获取真实IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            // 处理本地 IPv6 地址
            if ("0:0:0:0:0:0:0:1".equals(ip)) {
                ip = "127.0.0.1";
            }
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }
}
