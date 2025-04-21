package com.qingmeng.smartpictureku.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.mapper.SpaceMapper;
import com.qingmeng.smartpictureku.model.dto.space.SpaceAddRequest;
import com.qingmeng.smartpictureku.model.dto.space.SpaceQueryRequest;
import com.qingmeng.smartpictureku.model.entity.Space;
import com.qingmeng.smartpictureku.model.entity.SpaceUser;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.enums.SpaceLevelEnum;
import com.qingmeng.smartpictureku.model.enums.SpaceRoleEnum;
import com.qingmeng.smartpictureku.model.enums.SpaceTypeEnum;
import com.qingmeng.smartpictureku.model.vo.SpaceVO;
import com.qingmeng.smartpictureku.service.SpaceService;
import com.qingmeng.smartpictureku.service.SpaceUserService;
import com.qingmeng.smartpictureku.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Wang
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-03-10 14:11:50
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private SpaceUserService spaceUserService;

    //@Resource
    //@Lazy
    //private DynamicShardingManager dynamicShardingManager;

    // 使用 ConcurrentHashMap 来管理锁对象
    private final Map<Long, Object> lockMap = new ConcurrentHashMap<>();

    /**
     * 创建空间
     *
     * @param spaceAddRequest
     */
    @Override
    public long createSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 1.填充参数,默认值
        Space space = buildSpace(spaceAddRequest, loginUser);
        // 2.校验参数
        this.validSpace(space, true);
        // 3.校验权限,非管理员只能创建普通级别的空间
        if (SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "目前只开放创建普通空间");
        }
        Long userId = loginUser.getId();
        // 4. 控制同一用户只能创建一个私有空间
        // 针对用户加锁,这里加锁是防止用户快速点击,导致的创建多个空间
        // 这里定义了一个ConcurrentHashMap,线程安全的,根据userId来获取锁对象
        Object lock = lockMap.computeIfAbsent(userId, key -> new Object());

        synchronized (lock) {
            try {
                Long newSpaceId = transactionTemplate.execute(status -> {
                    // 判断是否已有空间
                    boolean exists = this.lambdaQuery()
                            .eq(Space::getUserId, userId)
                            // 添加空间类型
                            .eq(Space::getSpaceType, space.getSpaceType())
                            .exists();
                    // 如果已有空间，就不能再创建
                    if (exists && space.getSpaceType() == SpaceTypeEnum.PRIVATE.getValue()) {
                        throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户只能创建一个私有空间");
                    }
                    if (exists && space.getSpaceType() == SpaceTypeEnum.TEAM.getValue()) {
                        throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户只能创建一个团队空间");
                    }
                    // 操作数据库
                    boolean result = this.save(space);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存空间到数据库失败");
                    // 如果是团队空间,关联创建空间成员记录
                    if (space.getSpaceType() == SpaceTypeEnum.TEAM.getValue()) {
                        SpaceUser spaceUser = new SpaceUser();
                        spaceUser.setSpaceId(space.getId());
                        spaceUser.setUserId(userId);
                        spaceUser.setSpaceRole(SpaceRoleEnum.ADMINER.getValue());
                        boolean save = spaceUserService.save(spaceUser);
                        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败");
                    }
                    // 创建分表
                    //dynamicShardingManager.createSpacePictureTable(space);
                    // 返回新写入的数据 id
                    return space.getId();
                });
                return Optional.ofNullable(newSpaceId).orElse(-1L);
            } finally {
                // 防止内存泄漏，移除锁对象
                lockMap.remove(userId);
            }
        }
    }

    /**
     * 创建空间时构建空间对象
     *
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    private Space buildSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // DTO 转换为 实体类
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        if (space.getSpaceName() == null) {
            if (space.getSpaceType() == SpaceTypeEnum.TEAM.getValue()) {
                space.setSpaceName(StrUtil.format("{}的团队空间", loginUser.getUserName()));
            } else {
                space.setSpaceName(StrUtil.format("{}的私人空间", loginUser.getUserName()));
            }
        }
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        // 补充空间类型
        if (space.getSpaceType() == null) {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        // 填充容量和大小
        this.fileSpaceBySpaceLevel(space);
        space.setUserId(loginUser.getId());
        return space;
    }

    /**
     * 获取查询条件
     *
     * @param spaceQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper<Space> spaceQueryWrapper = new QueryWrapper<>();

        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        Integer spaceType = spaceQueryRequest.getSpaceType();


        // 构造查询条件
        spaceQueryWrapper.lambda()
                .eq(ObjUtil.isNotEmpty(id), Space::getId, id)
                .eq(ObjUtil.isNotEmpty(userId), Space::getUserId, userId)
                .eq(ObjUtil.isNotEmpty(spaceType), Space::getSpaceType, spaceType)
                .like(StrUtil.isNotBlank(spaceName), Space::getSpaceName, spaceName)
                .eq(ObjUtil.isNotEmpty(spaceLevel), Space::getSpaceLevel, spaceLevel);
        // 排序
        spaceQueryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return spaceQueryWrapper;
    }

    /**
     * 获取空间封装对象SpaceVO
     *
     * @param space
     * @return
     */
    @Override
    public SpaceVO getSpaceVO(Space space) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = spaceVO.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                spaceVO.setUserVO(userService.getUserVO(user));
            }
        }
        return spaceVO;
    }

    /**
     * 获取分页查询结果(封装后的)
     *
     * @param spacePage
     * @param request
     * @return
     */
    @Override
    public Page<SpaceVO> getSpaceVoPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVoPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVoPage;
        }
        // 将空间对象列表 => 封装对象列表
        List<SpaceVO> spaceVoList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 关联查询用户信息
        // 获取用户id集合
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        // 批量查询用户信息
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 填充信息
        spaceVoList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUserVO(userService.getUserVO(user));
        });
        // 将封装对象列表 设置到 分页对象中,并返回
        spaceVoPage.setRecords(spaceVoList);
        return spaceVoPage;
    }

    /**
     * 校验空间
     *
     * @param space
     */
    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        Integer spaceType = space.getSpaceType();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        if (add) {
            // 创建空间
            if (StringUtils.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
            if (spaceType == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不能为空");
            }

        }
        // 修改数据时,如果要该空间级别
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
        // 修改数据时，如果要改空间级别
        if (spaceType != null && spaceTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不存在");
        }
    }

    /**
     * 根据空间级别填充空间限额
     *
     * @param space
     */
    @Override
    public void fileSpaceBySpaceLevel(Space space) {
        // 根据空间级别,自动填充限额
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxCount = spaceLevelEnum.getMaxCount();
            long maxSize = spaceLevelEnum.getMaxSize();
            // 如果空间没有设置才会填充
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
        }
    }

    /**
     * 校验空间权限
     *
     * @param space
     * @param loginUser
     */
    @Override
    public void checkSpaceAuth(Space space, User loginUser) {
        if (!userService.isAdmin(loginUser) && !space.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }

    @Override
    public SpaceVO getSpaceByUserId(Long userId) {
        QueryWrapper<Space> spaceQueryWrapper = new QueryWrapper<Space>()
                .eq("userId", userId)
                .eq("spaceType", 0);
        Space space = this.getOne(spaceQueryWrapper);
        if (space == null){
            return null;
        }
        return SpaceVO.objToVo(space);
    }

}




