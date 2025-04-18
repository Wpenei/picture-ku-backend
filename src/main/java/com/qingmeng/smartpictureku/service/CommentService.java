package com.qingmeng.smartpictureku.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingmeng.smartpictureku.model.dto.comment.CommentAddRequest;
import com.qingmeng.smartpictureku.model.dto.comment.CommentLikeRequest;
import com.qingmeng.smartpictureku.model.dto.comment.CommentQueryRequest;
import com.qingmeng.smartpictureku.model.entity.Comment;
import com.qingmeng.smartpictureku.model.vo.CommentVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Wang
* @description 针对表【comments(评论表)】的数据库操作Service
* @createDate 2025-04-18 12:27:59
*/
public interface CommentService extends IService<Comment> {

    /**
     * 添加评论
     */
    Boolean addComment(CommentAddRequest commentAddRequest, HttpServletRequest request);

    /**
     * 删除评论
     */
    Boolean deleteComment(Long id, HttpServletRequest request);

    /**
     * 查询评论
     */
    Page<CommentVO> queryComment(CommentQueryRequest commentsQueryRequest, HttpServletRequest request);

    /**
     * 点赞评论
     */
    Boolean likeComment(CommentLikeRequest commentsLikeRequest, HttpServletRequest request);

    /**
     * 获取并清除用户未读的评论消息
     *
     * @param userId 用户ID
     * @return 未读的评论消息列表
     */
    List<CommentVO> getAndClearUnreadComments(Long userId);

    /**
     * 获取用户未读评论数
     */
    long getUnreadCommentsCount(Long userId);

    /**
     * 清除用户所有未读评论状态
     */
    void clearAllUnreadComments(Long userId);

    /**
     * 获取用户评论历史记录
     */
    Page<CommentVO> getCommentedHistory(CommentQueryRequest commentsQueryRequest, Long id);

    /**
     * 获取我的评论的历史记录
     */
    Page<CommentVO> getMyCommentHistory(CommentQueryRequest commentsQueryRequest, Long id);
}
