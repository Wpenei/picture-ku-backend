package com.qingmeng.smartpictureku.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.mapper.LikeRecordMapper;
import com.qingmeng.smartpictureku.model.dto.like.LikeQueryRequest;
import com.qingmeng.smartpictureku.model.dto.like.LikeRequest;
import com.qingmeng.smartpictureku.model.entity.Comment;
import com.qingmeng.smartpictureku.model.entity.LikeRecord;
import com.qingmeng.smartpictureku.model.entity.Picture;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.vo.CommentVO;
import com.qingmeng.smartpictureku.model.vo.LikeRecordVO;
import com.qingmeng.smartpictureku.model.vo.PictureVO;
import com.qingmeng.smartpictureku.service.CommentService;
import com.qingmeng.smartpictureku.service.LikeRecordService;
import com.qingmeng.smartpictureku.service.PictureService;
import com.qingmeng.smartpictureku.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author Wang
 * @description 针对表【like_record(通用点赞表)】的数据库操作Service实现
 * @createDate 2025-04-17 18:49:06
 */
@Service
@Slf4j
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord>
        implements LikeRecordService {

    @Lazy
    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private CommentService commentService;
    @Resource
    private Executor pictureKuExecutor;

    /**
     * 点赞/取消点赞
     * @param likeRequest
     * @param userId
     * @return
     */
    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public CompletableFuture<Boolean> doLike(LikeRequest likeRequest, Long userId) {
        try {
            // 1.校验参数
            ThrowUtils.throwIf(likeRequest == null, ErrorCode.PARAMS_ERROR);
            Long targetId = likeRequest.getTargetId();
            Integer targetType = likeRequest.getTargetType();
            Boolean isLiked = likeRequest.getIsLiked();
            if (targetId == null || targetType == null || isLiked == null || userId == null) {
                log.error("Invalid parameters: targetId={}, targetType={}, isLiked={}, userId={}",
                        targetId, targetType, isLiked, userId);
                return CompletableFuture.completedFuture(false);
            }
            // 2.判断目标类型 1-图片 2-帖子 3-评论
            if (targetType != 1 && targetType != 2 && targetType != 3) {
                log.error("Invalid target type: targetType={}", targetType);
                return CompletableFuture.completedFuture(false);
            }
            // 3.根据目标ID和类型查询目标所属用户ID
            Long targetUserId = getTargetUserId(targetId, targetType);
            if (targetUserId == null) {
                log.error("Target content not found: targetId={}, targetType={}", targetId, targetType);
                return CompletableFuture.completedFuture(false);
            }
            // 查询当前点赞状态
            QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userId", userId)
                    .eq("targetId", targetId)
                    .eq("targetType", targetType);
            LikeRecord oldLikeRecord = this.getOne(queryWrapper);
            // 如果没有点赞数据且正在点赞，即为首次点赞
            if (oldLikeRecord == null) {
                // 新增点赞记录
                if (isLiked) {
                    // 首次点赞
                    LikeRecord likeRecord = new LikeRecord();
                    likeRecord.setUserId(userId);
                    likeRecord.setTargetId(targetId);
                    likeRecord.setTargetType(targetType);
                    likeRecord.setTargetUserId(targetUserId);
                    likeRecord.setIsLiked(true);
                    likeRecord.setFirstLikeTime(new Date());
                    likeRecord.setLastLikeTime(new Date());
                    likeRecord.setIsRead(0);
                    this.save(likeRecord);
                    updateLikeCount(targetId, targetType, 1);
                }
            } else {
                // 非首次点赞，若原状态与现状态不一致，则更改其状态
                if (!isLiked.equals(oldLikeRecord.getIsLiked())) {
                    // 更新点赞状态
                    oldLikeRecord.setIsLiked(isLiked);
                    oldLikeRecord.setLastLikeTime(new Date());
                    // 更新目标内容所属用户ID
                    oldLikeRecord.setTargetUserId(targetUserId);
                    // 如果是重新点赞，设置为未读
                    if (isLiked) {
                        oldLikeRecord.setIsRead(0);
                    }
                    this.updateById(oldLikeRecord);
                    updateLikeCount(targetId, targetType, isLiked ? 1 : -1);
                }
            }
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("Error doing like: ", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 获取目标内容所属用户ID
     *
     * @param targetId   目标id
     * @param targetType 目标类型
     * @return 目标内容所属用户id
     */
    private Long getTargetUserId(Long targetId, Integer targetType) {
        try {
            switch (targetType) {
                // 图片
                case 1:
                    Picture picture = pictureService.getById(targetId);
                    return picture != null ? picture.getUserId() : null;
                // 帖子
                //case 2:
                //    Post post = postService.getById(targetId);
                //    return post != null ? post.getUserId() : null;
                // 图片
                case 3:
                    Comment comment = commentService.getById(targetId);
                    return comment != null ? comment.getUserId() : null;
                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("Error getting target user id: ", e);
            return null;
        }
    }

    /**
     * 更新点赞数
     *
     * @param targetId   目标id
     * @param targetType 目标类型
     * @param delta      变化量
     */
    private void updateLikeCount(Long targetId, Integer targetType, int delta) {
        switch (targetType) {
            // 图片
            case 1:
                pictureService.update()
                        .setSql("likeCount = likeCount + " + delta)
                        .eq("id", targetId)
                        .ge("likeCount", -delta)
                        .update();
                //updateEsPictureLikeCount(targetId, delta);
                break;
            //case 2: // 帖子
            //    postService.update()
            //            .setSql("likeCount = likeCount + " + delta)
            //            .eq("id", targetId)
            //            .ge("likeCount", -delta)
            //            .update();
            //    updateEsPostLikeCount(targetId, delta);
            //    break;
            // 图片
            case 3:
                commentService.update()
                        .setSql("likeCount = likeCount + " + delta)
                        .eq("id", targetId)
                        .ge("likeCount", -delta)
                        .update();
            default:
                log.error("Unsupported target type: {}", targetType);
        }
    }

    // region ES

    /**
     * 更新 ES 中图片的点赞数
     * @param pictureId 图片id
     * @param delta 变化量
     */
    //private void updateEsPictureLikeCount(Long pictureId, int delta) {
    //    try {
    //        esPictureDao.findById(pictureId).ifPresent(esPicture -> {
    //            esPicture.setLikeCount(esPicture.getLikeCount() + delta);
    //            esPictureDao.save(esPicture);
    //        });
    //    } catch (Exception e) {
    //        log.error("Failed to update ES picture like count, pictureId: {}", pictureId, e);
    //    }
    //}

    /**
     * 更新 ES 中帖子的点赞数
     * @param postId 帖子id
     * @param delta 变化量
     */
    //private void updateEsPostLikeCount(Long postId, int delta) {
    //    try {
    //        esPostDao.findById(postId).ifPresent(esPost -> {
    //            esPost.setLikeCount(esPost.getLikeCount() + delta);
    //            esPostDao.save(esPost);
    //        });
    //    } catch (Exception e) {
    //        log.error("Failed to update ES post like count, postId: {}", postId, e);
    //    }
    //}

    //endregion

    /**
     * 获取并清除用户未读的点赞消息
     * @param userId 当前用户id
     * @return List<LikeRecordVO>
     */
    @Override
    @Transactional
    public List<LikeRecordVO> getAndClearUnreadLikes(Long userId) {
        // 1. 获取未读点赞记录
        QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("targetUserId", userId)
                .eq("isRead", 0)
                .eq("isLiked", true)
                .ne("userId", userId)
                .orderByDesc("lastLikeTime")
                // 限制最多返回50条数据
                .last("LIMIT 50");

        // 获取到用户未读的点赞记录列表
        List<LikeRecord> unreadLikes = this.list(queryWrapper);
        if (CollUtil.isEmpty(unreadLikes)) {
            return new ArrayList<>();
        }

        // 2. 批量更新为已读
        // 获取为点赞的记录id列表
        List<Long> likeIds = unreadLikes.stream()
                .map(LikeRecord::getId)
                .collect(Collectors.toList());
        // 根据id列表批量修改点赞记录
        this.update(new UpdateWrapper<LikeRecord>()
                .set("isRead", 1)
                .in("id", likeIds));
        // 返回未读的点赞记录列表-脱敏
        return convertToVOList(unreadLikes);
    }

    /**
     * 点赞列表脱敏
     * @param likeRecords
     * @return
     */
    private List<LikeRecordVO> convertToVOList(List<LikeRecord> likeRecords) {
        if (CollUtil.isEmpty(likeRecords)) {
            return new ArrayList<>();
        }

        return likeRecords.stream().map(like -> {
            LikeRecordVO vo = new LikeRecordVO();
            BeanUtils.copyProperties(like, vo);

            // 设置点赞用户信息
            User likeUser = userService.getById(like.getUserId());
            if (likeUser != null) {
                vo.setUser(userService.getUserVO(likeUser));
            }

            // 根据类型获取目标内容
            switch (like.getTargetType()) {
                case 1: // 图片
                    Picture picture = pictureService.getById(like.getTargetId());
                    if (picture != null) {
                        PictureVO pictureVO = PictureVO.objToVo(picture);
                        // 设置图片作者信息
                        User pictureUser = userService.getById(picture.getUserId());
                        if (pictureUser != null) {
                            pictureVO.setUserVO(userService.getUserVO(pictureUser));
                        }
                        vo.setTarget(pictureVO);
                    }
                    break;
                //case 2: // 帖子
                //    Post post = postService.getById(like.getTargetId());
                //    if (post != null) {
                //        // 设置帖子作者信息
                //        User postUser = userService.getById(post.getUserId());
                //        if (postUser != null) {
                //            post.setUser(userService.getUserVO(postUser));
                //        }
                //        vo.setTarget(post);
                //    }
                //    break;
                // 图片
                case 3:
                    Comment comment = commentService.getById(like.getTargetId());
                    if (comment != null) {
                        CommentVO commentVO = new CommentVO();
                        BeanUtil.copyProperties(comment,commentVO);
                        vo.setTarget(commentVO);
                    }
                    break;
                default:
                    log.error("Unsupported target type: {}", like.getTargetType());
                    break;
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取用户点赞历史
     * @param likeQueryRequest 点赞查询请求
     * @param userId 用户id
     * @return Page<LikeRecordVO>
     */
    @Override
    public Page<LikeRecordVO> getUserLikeHistory(LikeQueryRequest likeQueryRequest, Long userId) {
        // 1. 校验参数
        ThrowUtils.throwIf(likeQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = likeQueryRequest.getCurrent();
        int pageSize = likeQueryRequest.getPageSize();
        // 创建分页对象
        Page<LikeRecord> page = new Page<>(current, pageSize);
        // 构建查询条件
        QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
        // 查询被点赞的记录
        queryWrapper.eq("targetUserId", userId)
                // 排除自己点赞自己的记录;
                .ne("userId", userId);
        // 处理目标类型查询
        Integer targetType = likeQueryRequest.getTargetType();
        if (targetType != null) {
            queryWrapper.eq("targetType", targetType);
        }
        queryWrapper.orderByDesc("lastLikeTime");

        // 执行分页查询
        Page<LikeRecord> likePage = this.page(page, queryWrapper);

        // 转换结果
        List<LikeRecordVO> records = convertToVOList(likePage.getRecords());

        // 构建返回结果
        Page<LikeRecordVO> voPage = new Page<>(likePage.getCurrent(), likePage.getSize(), likePage.getTotal());
        voPage.setRecords(records);

        return voPage;
    }

    /**
     * 检查内容是否已被用户点赞
     * @param targetId 目标id
     * @param targetType 目标类型
     * @param userId 用户id
     * @return 判断结果
     */
    @Override
    public boolean isContentLiked(Long targetId, Integer targetType, Long userId) {
        QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("targetId", targetId)
                .eq("targetType", targetType)
                .eq("userId", userId)
                .eq("isLiked", true);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 统计未读点赞数
     * @param userId 用户id
     * @return 未读点赞数
     */
    @Override
    public long getUnreadLikesCount(Long userId) {
        return this.count(new QueryWrapper<LikeRecord>()
                .eq("targetUserId", userId)
                .eq("isRead", 0)
                .eq("isLiked", true)
                .ne("userId", userId));
    }

    /**
     * 全部已读
     * @param userId 用户id
     */
    @Override
    public void clearAllUnreadLikes(Long userId) {
        this.update(new UpdateWrapper<LikeRecord>()
                .set("isRead", 1)
                .eq("targetUserId", userId)
                .eq("isRead", 0)
                .eq("isLiked", true));
    }

    /**
     * 获取我的点赞历史
     * @param likeQueryRequest 点赞查询请求
     * @param userId 用户id
     * @return 点赞信息-脱敏-分页
     */
    @Override
    public Page<LikeRecordVO> getMyLikeHistory(LikeQueryRequest likeQueryRequest, Long userId) {
        long current = likeQueryRequest.getCurrent();
        long size = likeQueryRequest.getPageSize();

        // 创建分页对象
        Page<LikeRecord> page = new Page<>(current, size);

        // 构建查询条件
        QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
        // 查询用户自己的点赞记录
        queryWrapper.eq("userId", userId)
                // 只查询点赞状态为true的记录
                .eq("isLiked", true);

        // 处理目标类型查询
        Integer targetType = likeQueryRequest.getTargetType();
        if (targetType != null) {
            queryWrapper.eq("targetType", targetType);
        }

        queryWrapper.orderByDesc("lastLikeTime");

        // 执行分页查询
        Page<LikeRecord> likePage = this.page(page, queryWrapper);

        // 转换结果
        List<LikeRecordVO> records = convertToVOList(likePage.getRecords());

        // 构建返回结果
        Page<LikeRecordVO> voPage = new Page<>(likePage.getCurrent(), likePage.getSize(), likePage.getTotal());
        voPage.setRecords(records);

        return voPage;
    }
}




