package com.qingmeng.smartpictureku.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingmeng.smartpictureku.common.BaseResponse;
import com.qingmeng.smartpictureku.common.DeleteRequest;
import com.qingmeng.smartpictureku.common.ResultUtils;
import com.qingmeng.smartpictureku.constant.CrawlerConstant;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.model.dto.comment.CommentAddRequest;
import com.qingmeng.smartpictureku.model.dto.comment.CommentLikeRequest;
import com.qingmeng.smartpictureku.model.dto.comment.CommentQueryRequest;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.vo.CommentVO;
import com.qingmeng.smartpictureku.service.CommentService;
import com.qingmeng.smartpictureku.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * &#064;description:图片接口
 *
 * @author Wang
 * &#064;date: 2025/4/18
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private UserService userService;

    @Resource
    private CommentService commentsService;

    /**
     * 添加评论
     * @param commentAddRequest 评论内容请求
     * @param request HTTP请求
     * @return 添加结果
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addComment(@RequestBody CommentAddRequest commentAddRequest, HttpServletRequest request) {
        // 检测高频操作
        //crawlerManager.detectFrequentRequest(request);
        return ResultUtils.success(commentsService.addComment(commentAddRequest, request));
    }

    /**
     * 删除评论
     * @param commentsDeleteRequest 删除评论请求
     * @param request HTTP请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteComment(@RequestBody DeleteRequest commentsDeleteRequest, HttpServletRequest request) {
        // 检测高频操作
        //crawlerManager.detectFrequentRequest(request);
        if (commentsDeleteRequest == null || commentsDeleteRequest.getId() == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(commentsService.deleteComment(commentsDeleteRequest.getId(), request));
    }

    /**
     * 查询指定图片的评论列表
     * @param commentsQueryRequest 评论查询参数
     * @param request HTTP请求
     * @return 评论列表（分页）
     */
    @PostMapping("/query")
    public BaseResponse<Page<CommentVO>> queryComment(@RequestBody CommentQueryRequest commentsQueryRequest, HttpServletRequest request) {
        // 用户权限校验
        User loginUser = userService.getLoginUser(request);
        if (loginUser != null) {
            String userRole = loginUser.getUserRole();
            ThrowUtils.throwIf(userRole.equals(CrawlerConstant.BAN_ROLE),
                    ErrorCode.NO_AUTH_ERROR, "封禁用户禁止获取数据,请联系管理员");
        }

        // 限制爬虫
        long size = commentsQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        //crawlerManager.detectNormalRequest(request);

        return ResultUtils.success(commentsService.queryComment(commentsQueryRequest, request));
    }

    /**
     * 点赞评论
     * @param commentlikeRequest 评论点赞请求
     * @param request HTTP请求
     * @return 点赞结果
     */
    @PostMapping("/like")
    @Deprecated // TODO 因为有通用点赞表,暂时不使用，若有问题再使用
    public BaseResponse<Boolean> likeComment(@RequestBody CommentLikeRequest commentlikeRequest, HttpServletRequest request) {
        // 检测高频操作
        //crawlerManager.detectFrequentRequest(request);
        return ResultUtils.success(commentsService.likeComment(commentlikeRequest, request));
    }

    /**
     * 获取未读评论列表
     * @param request HTTP请求
     * @return 未读评论列表
     */
    @GetMapping("/unread")
    public BaseResponse<List<CommentVO>> getUnreadComments(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 检测普通请求
        //crawlerManager.detectNormalRequest(request);

        List<CommentVO> unreadComments = commentsService.getAndClearUnreadComments(loginUser.getId());
        return ResultUtils.success(unreadComments);
    }

    /**
     * 获取未读评论数量
     * @param request HTTP请求
     * @return 未读评论数量
     */
    @GetMapping("/unread/count")
    public BaseResponse<Long> getUnreadCommentsCount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 检测普通请求
        //crawlerManager.detectNormalRequest(request);

        return ResultUtils.success(commentsService.getUnreadCommentsCount(loginUser.getId()));
    }


    /**
     * 获取评论我的历史
     * @param commentsQueryRequest 评论查询参数
     * @param request HTTP请求
     * @return 评论我的历史（分页）
     */
    @PostMapping("/commented/history")
    public BaseResponse<Page<CommentVO>> getCommentedHistory(@RequestBody CommentQueryRequest commentsQueryRequest,
                                                              HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 限制爬虫
        long size = commentsQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //crawlerManager.detectNormalRequest(request);

        Page<CommentVO> commentHistory = commentsService.getCommentedHistory(commentsQueryRequest, loginUser.getId());
        return ResultUtils.success(commentHistory);
    }

    /**
     * 获取我的评论历史
     * @param commentsQueryRequest 评论查询参数
     * @param request HTTP请求
     * @return 我的评论历史（分页）
     */
    @PostMapping("/my/history")
    public BaseResponse<Page<CommentVO>> getMyCommentHistory(@RequestBody CommentQueryRequest commentsQueryRequest,
                                                             HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 限制爬虫
        long size = commentsQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //crawlerManager.detectNormalRequest(request);

        Page<CommentVO> commentHistory = commentsService.getMyCommentHistory(commentsQueryRequest, loginUser.getId());
        return ResultUtils.success(commentHistory);
    }
}
