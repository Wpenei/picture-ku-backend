package com.qingmeng.smartpictureku.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.mapper.CommentMapper;
import com.qingmeng.smartpictureku.model.dto.comment.CommentAddRequest;
import com.qingmeng.smartpictureku.model.dto.comment.CommentLikeRequest;
import com.qingmeng.smartpictureku.model.dto.comment.CommentQueryRequest;
import com.qingmeng.smartpictureku.model.entity.Comment;
import com.qingmeng.smartpictureku.model.entity.Picture;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.vo.CommentUserVO;
import com.qingmeng.smartpictureku.model.vo.CommentVO;
import com.qingmeng.smartpictureku.model.vo.PictureVO;
import com.qingmeng.smartpictureku.service.CommentService;
import com.qingmeng.smartpictureku.service.PictureService;
import com.qingmeng.smartpictureku.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Wang
 * @description 针对表【comments(评论表)】的数据库操作Service实现
 * @createDate 2025-04-18 12:27:59
 */
@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private PictureService pictureService;

    //@Resource
    //private PostService postService;

    /**
     * 添加评论
     *
     * @param commentAddRequest 添加评论对象
     * @param request           请求
     * @return 添加结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addComment(CommentAddRequest commentAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(commentAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 校验用户是否登录
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 获取目标内容所属用户id
        Long targetId = commentAddRequest.getTargetId();
        Integer targetType = commentAddRequest.getTargetType();
        ThrowUtils.throwIf(targetId == null || targetType == null, ErrorCode.PARAMS_ERROR, "评论参数错误");
        Long targetUserId;
        switch (targetType) {
            // 图片
            case 1:
                Picture picture = pictureService.getById(targetId);
                ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
                targetUserId = picture.getUserId();
                break;
            // 帖子
            //case 2:
            //    Post post = postService.getById(commentsAddRequest.getTargetId());
            //    ThrowUtils.throwIf(post == null,ErrorCode.NOT_FOUND_ERROR, "帖子不存在");
            //    targetUserId = post.getUserId();
            //    break;
            // 评论
            case 3:
                Comment comment = this.getById(targetId);
                ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR, "评论不存在");
                targetUserId = comment.getUserId();
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的评论类型");
        }
        Comment comment = new Comment();
        BeanUtil.copyProperties(commentAddRequest, comment);
        comment.setUserId(loginUser.getId());
        comment.setTargetUserId(targetUserId);
        comment.setLikeCount(0L);
        comment.setDislikeCount(0L);
        comment.setIsRead(0);
        comment.setIsDelete(0);
        comment.setCommentCount(0L);
        // 如果评论的是别人的评论，则将父评论ID设置为目标id
        if (targetType == 3) {
            comment.setParentCommentId(targetId);
        } else {
            // 若不是，则设为0，顶级评论
            comment.setParentCommentId(0L);
        }
        boolean save = this.save(comment);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "评论失败");
        }
        // 更新评论数
        updateCommentCount(targetId, targetType, 1L, false);
        return true;
    }


    /**
     * 更新评论数
     */
    private void updateCommentCount(Long targetId, Integer targetType, Long delta, Boolean isOpposite) {
        if (isOpposite) {
            delta = -delta;
        }
        switch (targetType) {
            // 图片
            case 1:
                pictureService.update()
                        .setSql("commentCount = commentCount + " + delta)
                        .eq("id", targetId)
                        .ge("commentCount", -delta)
                        .update();
                //updateEsPictureCommentCount(targetId, delta);
                break;
            // 帖子
            //case 2:
            //    postService.update()
            //            .setSql("commentCount = commentCount + " + delta)
            //            .eq("id", targetId)
            //            .ge("commentCount", -delta)
            //            .update();
            //    updateEsPostCommentCount(targetId, delta);
            //    break;
            // 评论
            case 3:
                // 递归查找原始的评论类型是图片还是帖子，为其添加评论数
                Comment comment = this.getById(targetId);
                if (comment != null) {
                    updateCommentCount(comment.getTargetId(), comment.getTargetType(), delta, false);
                }
                this.update()
                        .setSql("commentCount = commentCount + " + delta)
                        .eq("id", targetId)
                        .ge("commentCount", -delta)
                        .update();
                //updateEsPostCommentCount(targetId, delta);
                break;
            default:
                log.error("Unsupported target type: {}", targetType);
        }
    }

    /**
     * 删除评论
     *
     * @param id      删除评论id
     * @param request 请求
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteComment(Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 获取评论信息
        Comment comment = this.getById(id);
        if (comment == null || comment.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }
        // 判断是否为管理员或者评论所属用户
        if (!comment.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除评论");
        }
        // 先计算要删除的评论及其子评论的总数
        //int deletedCommentCount = countCommentsRecursively(comment.getId());
        Long deletedCommentCount = comment.getCommentCount() + 1;
        // 递归删除评论及其子评论
        boolean success = deleteCommentsRecursively(comment.getId());
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除评论失败");
        }
        // 更新 MySQL 中的评论数
        updateCommentCount(comment.getTargetId(), comment.getTargetType(), deletedCommentCount, true);
        return true;
    }

    /**
     * 递归删除评论及其子评论
     */
    private boolean deleteCommentsRecursively(Long commentId) {
        // 获取所有未删除的子评论
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parentCommentId", commentId)
                .eq("isDelete", 0);
        List<Comment> childComments = this.list(queryWrapper);

        // 递归删除子评论
        for (Comment childComment : childComments) {
            if (!deleteCommentsRecursively(childComment.getId())) {
                return false;
            }
        }

        // 删除当前评论
        return this.update(new UpdateWrapper<Comment>()
                .eq("id", commentId)
                .eq("isDelete", 0)
                .set("isDelete", 1));
    }

    /**
     * 递归计算评论及其子评论的数量
     */
    private int countCommentsRecursively(Long commentId) {
        // 获取所有未删除的子评论
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parentCommentId", commentId)
                .eq("isDelete", 0);
        List<Comment> childComments = this.list(queryWrapper);

        // 计算子评论总数
        // 当前评论
        int count = 1;
        if (!childComments.isEmpty()) {
            for (Comment childComment : childComments) {
                count += countCommentsRecursively(childComment.getId());
            }
        }
        return count;
    }


    /**
     * 查询评论
     *
     * @param commentsQueryRequest 评论查询对象
     * @param request              请求
     * @return 评论列表-分页-脱敏
     */
    @Override
    public Page<CommentVO> queryComment(CommentQueryRequest commentsQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        int current = commentsQueryRequest.getCurrent();
        int pageSize = commentsQueryRequest.getPageSize();
        Page<Comment> page = new Page<>(current, pageSize);
        //判断是否传递图片id
        ThrowUtils.throwIf(commentsQueryRequest.getTargetId() == null , ErrorCode.PARAMS_ERROR, "目标id不能为空");
        ThrowUtils.throwIf( commentsQueryRequest.getTargetType() == null, ErrorCode.PARAMS_ERROR, "目标类型不能为空");
        // 构建查询条件，只查询顶级评论
        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.eq("targetId", commentsQueryRequest.getTargetId())
                .eq("targetType", commentsQueryRequest.getTargetType() != null ? commentsQueryRequest.getTargetType() : 1)
                .eq("parentCommentId", 0)
                .orderByAsc("createTime");
        // 查询评论是否存在，不存在返回空
        long count = this.count(commentQueryWrapper);
        // 得到顶级评论列表
        Page<Comment> commentsPage = this.page(page, commentQueryWrapper);
        List<Long> userIds = commentsPage.getRecords().stream()
                .map(Comment::getUserId)
                .toList();
        if (userIds.isEmpty()) {
            return new PageDTO<>(commentsPage.getCurrent(), commentsPage.getSize(), commentsPage.getTotal());
        }
        // 根据id列表查询用户信息列表，并将其转换为评论用户响应对象列表
        Map<Long, CommentUserVO> commentUserVoMap = userService.listByIds(userIds).stream()
                .map(user -> {
                    CommentUserVO commentUserVO = new CommentUserVO();
                    BeanUtils.copyProperties(user, commentUserVO);
                    return commentUserVO;
                }).collect(Collectors.toMap(CommentUserVO::getId, commentUserVO -> commentUserVO));
        // 将评论列表脱敏
        List<CommentVO> commentVOList = commentsPage.getRecords().stream()
                .map(comment -> {
                    CommentVO commentVO = new CommentVO();
                    BeanUtils.copyProperties(comment, commentVO);
                    CommentUserVO commentUserVO = commentUserVoMap.get(comment.getUserId());
                    if (commentUserVO != null) {
                        commentVO.setCommentUser(commentUserVO);
                    }
                    // 递归查询子评论
                    commentVO.setChildren(queryChildComments(comment.getId()));
                    return commentVO;
                }).toList();
        Page<CommentVO> resultPage = new PageDTO<>(commentsPage.getCurrent(), commentsPage.getSize(), commentsPage.getTotal());
        resultPage.setRecords(commentVOList);
        return resultPage;
    }

    /**
     * 递归查询子评论信息
     * @param parentCommentId 父评论id
     * @return 子评论列表
     */
    private List<CommentVO> queryChildComments(Long parentCommentId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parentCommentId", parentCommentId);
        // 按照创建时间倒序排列
        queryWrapper.orderByAsc("createTime");
        // 使用 CommentsService 的 list 方法查询子评论
        List<Comment> childrenComments = this.list(queryWrapper);
        if (childrenComments == null || childrenComments.isEmpty()) {
            return Collections.emptyList();
        }
        // 获取子评论的用户 ID 列表
        List<Long> childUserIds = childrenComments.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toList());
        // 批量查询子评论的用户信息
        List<User> childUsers = userService.listByIds(childUserIds);
        List<CommentUserVO> childCommentUserVos = childUsers.stream().map(user -> {
            CommentUserVO commentUserVO = new CommentUserVO();
            BeanUtils.copyProperties(user, commentUserVO);
            return commentUserVO;
        }).toList();
        Map<Long, CommentUserVO> childUserMap = childCommentUserVos.stream()
                .collect(Collectors.toMap(CommentUserVO::getId, commentUserVO ->  commentUserVO));
        return childrenComments.stream().map(comments -> {
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(comments, commentVO);
            // 查找对应的子评论用户信息
            CommentUserVO commentUserVO = childUserMap.get(comments.getUserId());
            if (commentUserVO!= null) {
                commentVO.setCommentUser(commentUserVO);
            }
            // 递归调用，查询子评论的子评论
            commentVO.setChildren(queryChildComments(comments.getId()));
            return commentVO;
        }).collect(Collectors.toList());
    }

    /**
     * 点赞评论
     *
     * @param commentLikeRequest 评论点赞对象
     * @param request             请求
     * @return 点赞结果
     */
    @Override
    public Boolean likeComment(CommentLikeRequest commentLikeRequest, HttpServletRequest request) {
        // 检查评论 ID 是否为空
        ThrowUtils.throwIf(commentLikeRequest.getId() == null, ErrorCode.PARAMS_ERROR, "评论 id 不能为空");
        // 获取用户信息
        User user = (User) request.getSession().getAttribute("user");

        // 创建更新包装器
        UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", commentLikeRequest.getId());
        //判断评论是否getOne(updateWrapper);
        Comment comments = getOne(updateWrapper);
        if (comments == null) {
            return false;
        }
        if (commentLikeRequest.getLikeCount()!= null && commentLikeRequest.getLikeCount() != 0) {
            // 处理点赞操作
            updateWrapper.setSql("likeCount = likeCount + "+commentLikeRequest.getLikeCount());
        }
        if(commentLikeRequest.getDislikeCount()!= null&& commentLikeRequest.getDislikeCount() != 0) {
            // 处理点踩操作
            updateWrapper.setSql("dislikeCount = dislikeCount + "+commentLikeRequest.getDislikeCount());
        }
        // 执行更新操作
        return  update(updateWrapper);
    }

    /**
     * 获取并清理用户未读消息状态
     * @param userId 用户ID
     * @return
     */
    @Override
    @Transactional
    public List<CommentVO> getAndClearUnreadComments(Long userId) {
        // 1. 获取未读评论记录
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("targetUserId", userId)
                .eq("isRead", 0)
                // 添加这一行，排除自己评论自己的记录
                .ne("userId", userId)
                .orderByDesc("createTime");

        List<Comment> unreadComments = this.list(queryWrapper);
        if (CollUtil.isEmpty(unreadComments)) {
            return new ArrayList<>();
        }

        // 2. 批量更新为已读
        List<Long> commentIds = unreadComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        this.update(new UpdateWrapper<Comment>()
                .set("isRead", 1)
                .in("commentId", commentIds));

        // 3. 构建返回数据
        return unreadComments.stream().map(comment -> {
            CommentVO commentsVO = new CommentVO();
            BeanUtils.copyProperties(comment, commentsVO);

            // 获取评论用户信息
            User commentUser = userService.getById(comment.getUserId());
            if (commentUser != null) {
                CommentUserVO commentUserVO = new CommentUserVO();
                BeanUtils.copyProperties(commentUser, commentUserVO);
                commentsVO.setCommentUser(commentUserVO);
            }

            // 根据目标类型获取不同内容
            switch (comment.getTargetType()) {
                case 1: // 图片
                    Picture picture = pictureService.getById(comment.getTargetId());
                    if (picture != null) {
                        commentsVO.setPicture(PictureVO.objToVo(picture));
                        // 设置图片作者信息
                        User pictureUser = userService.getById(picture.getUserId());
                        if (pictureUser != null) {
                            commentsVO.getPicture().setUserVO(userService.getUserVO(pictureUser));
                        }
                    }
                    break;
                //case 2: // 帖子
                //    Post post = postService.getById(comment.getTargetId());
                //    if (post != null) {
                //        // 设置帖子作者信息
                //        User postUser = userService.getById(post.getUserId());
                //        if (postUser != null) {
                //            post.setUser(userService.getUserVO(postUser));
                //        }
                //        commentsVO.setPost(post);
                //    }
                //    break;
                default:
                    log.error("Unsupported target type: {}", comment.getTargetType());
                    break;
            }

            // 递归获取子评论
            commentsVO.setChildren(queryChildComments(comment.getId()));

            return commentsVO;
        }).collect(Collectors.toList());
    }

    /**
     * 统计未读评论总数
     * @param userId 用户id
     * @return 未读评论总数
     */
    @Override
    public long getUnreadCommentsCount(Long userId) {
        return this.count(new QueryWrapper<Comment>()
                .eq("targetUserId", userId)
                .eq("isRead", 0)
                // 添加这一行，排除自己评论自己的记录
                .ne("userId", userId));
    }

    /**
     * 一键已读
     * @param userId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAllUnreadComments(Long userId) {
        this.update(new UpdateWrapper<Comment>()
                .set("isRead", 1)
                .eq("targetUserId", userId)
                .eq("isRead", 0));
    }

    /**
     * 获取用户评论的历史记录
     * @param commentsQueryRequest 评论查询对象
     * @param userId 用户id
     * @return 查询结果-分页-脱敏
     */
    @Override
    public Page<CommentVO> getCommentedHistory(CommentQueryRequest commentsQueryRequest, Long userId) {
        long current = commentsQueryRequest.getCurrent();
        long size = commentsQueryRequest.getPageSize();

        // 创建分页对象
        Page<Comment> page = new Page<>(current, size);

        // 构建查询条件
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        // 查询用户收到的评论
        queryWrapper.eq("targetUserId", userId)
                // 只查询未删除的评论
                .eq("isDelete", 0)
                // 排除自己评论自己的记录
                .ne("userId", userId);

        // 处理目标类型查询
        Integer targetType = commentsQueryRequest.getTargetType();
        if (targetType != null) {
            queryWrapper.eq("targetType", targetType);
        }

        queryWrapper.orderByDesc("createTime");

        // 执行分页查询
        Page<Comment> commentsPage = this.page(page, queryWrapper);

        // 转换结果
        List<CommentVO> records = commentsPage.getRecords().stream().map(comment -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(comment, vo);

            // 设置评论用户信息
            User commentUser = userService.getById(comment.getUserId());
            if (commentUser != null) {
                CommentUserVO commentUserVO = new CommentUserVO();
                BeanUtils.copyProperties(commentUser, commentUserVO);
                vo.setCommentUser(commentUserVO);
            }

            // 根据目标类型获取不同内容
            switch (comment.getTargetType()) {
                case 1: // 图片
                    Picture picture = pictureService.getById(comment.getTargetId());
                    if (picture != null) {
                        vo.setPicture(PictureVO.objToVo(picture));
                        // 设置图片作者信息
                        User pictureUser = userService.getById(picture.getUserId());
                        if (pictureUser != null) {
                            vo.getPicture().setUserVO(userService.getUserVO(pictureUser));
                        }
                    }
                    break;
                //case 2: // 帖子
                //    Post post = postService.getById(comment.getTargetId());
                //    if (post != null) {
                //        // 设置帖子作者信息
                //        User postUser = userService.getById(post.getUserId());
                //        if (postUser != null) {
                //            post.setUser(userService.getUserVO(postUser));
                //        }
                //        vo.setPost(post);
                //    }
                //    break;
                default:
                    log.error("Unsupported target type: {}", comment.getTargetType());
                    break;
            }

            return vo;
        }).collect(Collectors.toList());

        // 构建返回结果
        Page<CommentVO> voPage = new Page<>(commentsPage.getCurrent(), commentsPage.getSize(), commentsPage.getTotal());
        voPage.setRecords(records);

        return voPage;
    }

    /**
     * 获取我的评论的历史记录
     * @param commentsQueryRequest 评论查询对象
     * @param userId 用户id
     * @return 评论列表-脱敏-分页
     */
    @Override
    public Page<CommentVO> getMyCommentHistory(CommentQueryRequest commentsQueryRequest, Long userId) {
        long current = commentsQueryRequest.getCurrent();
        long size = commentsQueryRequest.getPageSize();

        // 创建分页对象
        Page<Comment> page = new Page<>(current, size);

        // 构建查询条件
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        // 查询用户发出的评论
        queryWrapper.eq("userId", userId)
                // 只查询未删除的评论
                .eq("isDelete", 0);

        // 处理目标类型查询
        Integer targetType = commentsQueryRequest.getTargetType();
        if (targetType != null) {
            queryWrapper.eq("targetType", targetType);
        }

        queryWrapper.orderByDesc("createTime");

        // 执行分页查询
        Page<Comment> commentsPage = this.page(page, queryWrapper);

        // 转换结果
        List<CommentVO> records = commentsPage.getRecords().stream().map(comment -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(comment, vo);

            // 设置评论用户信息
            User commentUser = userService.getById(comment.getUserId());
            if (commentUser != null) {
                CommentUserVO commentUserVO = new CommentUserVO();
                BeanUtils.copyProperties(commentUser, commentUserVO);
                vo.setCommentUser(commentUserVO);
            }

            // 根据目标类型获取不同内容
            switch (comment.getTargetType()) {
                case 1: // 图片
                    Picture picture = pictureService.getById(comment.getTargetId());
                    if (picture != null) {
                        vo.setPicture(PictureVO.objToVo(picture));
                        // 设置图片作者信息
                        User pictureUser = userService.getById(picture.getUserId());
                        if (pictureUser != null) {
                            vo.getPicture().setUserVO(userService.getUserVO(pictureUser));
                        }
                    }
                    break;
                //case 2: // 帖子
                //    Post post = postService.getById(comment.getTargetId());
                //    if (post != null) {
                //        // 设置帖子作者信息
                //        User postUser = userService.getById(post.getUserId());
                //        if (postUser != null) {
                //            post.setUser(userService.getUserVO(postUser));
                //        }
                //        vo.setPost(post);
                //    }
                //    break;
                default:
                    log.error("Unsupported target type: {}", comment.getTargetType());
                    break;
            }

            return vo;
        }).collect(Collectors.toList());

        // 构建返回结果
        Page<CommentVO> voPage = new Page<>(commentsPage.getCurrent(), commentsPage.getSize(), commentsPage.getTotal());
        voPage.setRecords(records);

        return voPage;

    }
}



