package com.qingmeng.smartpictureku.controller;

import com.qingmeng.smartpictureku.common.BaseResponse;
import com.qingmeng.smartpictureku.common.ResultUtils;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.vo.MessageCenterVO;
import com.qingmeng.smartpictureku.service.CommentService;
import com.qingmeng.smartpictureku.service.LikeRecordService;
import com.qingmeng.smartpictureku.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * &#064;description: 消息中心
 *
 * @author Wang
 * &#064;date: 2025/4/19
 */
@RestController
@RequestMapping("/message")
@Slf4j
public class MessageCenterController {

    @Resource
    private UserService userService;

    @Resource
    private CommentService commentService;

    @Resource
    private LikeRecordService likeRecordService;

    /**
     * 获取消息中心未读消息
     * @param request 请求
     * @return 未读消息总数
     */
    @GetMapping("/unread/count")
    public BaseResponse<MessageCenterVO> getUnReadMessage(HttpServletRequest request){
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null , ErrorCode.NOT_LOGIN_ERROR);
        MessageCenterVO messageCenterVO = new MessageCenterVO();
        // 获取各类型未读消息数
        long commentsCounts = commentService.getUnreadCommentsCount(loginUser.getId());
        long likesCounts = likeRecordService.getUnreadLikesCount(loginUser.getId());
        // 设置数据
        messageCenterVO.setUnreadComments(commentsCounts);
        messageCenterVO.setUnreadLikes(likesCounts);
        messageCenterVO.setTotalUnread(commentsCounts + likesCounts);
        return ResultUtils.success(messageCenterVO);
    }

    /**
     * 将所有消息已读
     * @param request 请求
     * @return 操作结果
     */
    @GetMapping("/setAllRead")
    public BaseResponse<Boolean> setAllMessageRead(HttpServletRequest request){
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null , ErrorCode.NOT_LOGIN_ERROR);
        // 将所有消息设置为已读
        try {
            commentService.clearAllUnreadComments(loginUser.getId());
            likeRecordService.clearAllUnreadLikes(loginUser.getId());
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error("Error in setAllMessageRead: ", e);
            return ResultUtils.success(false);
        }
    }
}
