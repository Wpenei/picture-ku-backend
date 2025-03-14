package com.qingmeng.smartpictureku.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.mapper.SpaceMapper;
import com.qingmeng.smartpictureku.model.dto.space.analyze.*;
import com.qingmeng.smartpictureku.model.entity.Picture;
import com.qingmeng.smartpictureku.model.entity.Space;
import com.qingmeng.smartpictureku.model.entity.User;
import com.qingmeng.smartpictureku.model.vo.analyze.*;
import com.qingmeng.smartpictureku.service.PictureService;
import com.qingmeng.smartpictureku.service.SpaceAnalyzeService;
import com.qingmeng.smartpictureku.service.SpaceService;
import com.qingmeng.smartpictureku.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Wang
 * @description 空间分析服务
 * @createDate 2025-03-10 14:11:50
 */
@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceAnalyzeService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;
    @Autowired
    private PictureService pictureService;

    /**
     * 校验空间分析权限
     *
     * @param spaceAnalyzeRequest
     * @param loginUser
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        if (spaceAnalyzeRequest.getQueryAll() || spaceAnalyzeRequest.getQueryPublic()) {
            // 全空间分析 / 公共空间分析 (仅管理员可以使用)
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "仅管理员可查看");
        } else {
            // 私有空间分析
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR, "空间id不能为空");
            Space space = this.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            spaceService.checkSpaceAuth(space, loginUser);
        }
    }

    /**
     * 根据范围填充查询对象
     *
     * @param spaceAnalyzeRequest
     * @param queryWrapper
     */
    private void fillSpaceAnalyzeRequest(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        if (spaceAnalyzeRequest.getQueryAll()) {
            return;
        } else if (spaceAnalyzeRequest.getQueryPublic()) {
            queryWrapper.isNull("spaceId");
            return;
        }
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }

    /**
     * 空间使用分析
     *
     * @param spaceUsageAnalyzeRequest
     * @param loginUser
     * @return
     */
    @Override
    public SpaceUsageAnalyzeResponse spaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
        if (spaceUsageAnalyzeRequest.getQueryPublic() || spaceUsageAnalyzeRequest.getQueryAll()) {
            // 公共空间分析或全部,判断用户是否为管理员
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "仅管理员可查看");
            // 构造查询条件
            QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
            pictureQueryWrapper.select("picSize");
            // 公共空间分析 false true
            if (!spaceUsageAnalyzeRequest.getQueryAll()) {
                pictureQueryWrapper.isNull("spaceId");
            }
            // 获取图片大小(
            List<Object> pictureObjList = pictureService.getBaseMapper().selectObjs(pictureQueryWrapper);
            // 将集合中的Long类型转换为long类型，并求和
            long useSize = pictureObjList.stream().mapToLong(result -> result instanceof Long ? (Long) result : 0L).sum();
            long useCount = pictureObjList.size();
            //封装返回结果
            spaceUsageAnalyzeResponse.setUserSize(useSize);
            spaceUsageAnalyzeResponse.setUserCount(useCount);
            // 公共图库无上限,无比例
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setSizeUsageRadio(null);
            spaceUsageAnalyzeResponse.setCountUsageRadio(null);
        } else {
            // 查询指定空间
            Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR, "空间id不能为空");
            // 获取空间信息
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");

            // 权限校验
            spaceService.checkSpaceAuth(space, loginUser);

            // 构造返回结果
            spaceUsageAnalyzeResponse.setUserSize(space.getTotalSize());
            spaceUsageAnalyzeResponse.setMaxSize(space.getMaxSize());
            // 获取直接计算,响应给前端 这里保留两位小数
            double sizeUsageRadio = NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue();
            spaceUsageAnalyzeResponse.setSizeUsageRadio(sizeUsageRadio);
            spaceUsageAnalyzeResponse.setUserCount(space.getTotalCount());
            spaceUsageAnalyzeResponse.setMaxCount(space.getMaxCount());
            double countUsageRadio = NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue();
            spaceUsageAnalyzeResponse.setCountUsageRadio(countUsageRadio);
        }
        return spaceUsageAnalyzeResponse;
    }

    /**
     * 空间图片分类分析
     *
     * @param spaceCategoryAnalyzeRequest
     * @param loginUser
     * @return
     */
    @Override
    public List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 权限校验
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 根据分析范围填充查询条件
        fillSpaceAnalyzeRequest(spaceCategoryAnalyzeRequest, queryWrapper);

        queryWrapper.select("category AS category",
                        "COUNT(*) AS count",
                        "SUM(picSize) AS totalSize")
                .groupBy("category");
        // 查询并转换结果
        return pictureService.getBaseMapper().selectMaps(queryWrapper)
                .stream().map(result -> {
                    String category = result.get("category") != null ? result.get("category").toString() : "未分类";
                    long count = ((Number) result.get("count")).longValue();
                    long totalSize = ((Number) result.get("totalSize")).longValue();
                    return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
                }).toList();
    }

    /**
     * 空间图片标签分析
     *
     * @param spaceTagAnalyzeRequest
     * @param loginUser
     * @return
     */
    @Override
    public List<SpaceTagAnalyzeResponse> spaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 权限校验
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillSpaceAnalyzeRequest(spaceTagAnalyzeRequest, queryWrapper);

        queryWrapper.select("tags");
        List<String> tagsJsonList = pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                // 过滤掉空值
                .filter(ObjUtil::isNotNull)
                // 将List转为字符串
                .map(Object::toString)
                .toList();
        // 合并所有标签,并排序
        Map<String, Long> tagCountMap = tagsJsonList.stream()
                // 将标签列表展平为一个字符串流
                .flatMap(tagsJson -> JSONUtil.toList(tagsJson, String.class).stream())
                // 统计每个标签的使用次数,并将结果收集到Map集合中
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));
        // 转换为响应对象,按使用次数降序排序
        List<SpaceTagAnalyzeResponse> spaceTagAnalyzeResponses = tagCountMap.entrySet().stream()
                // 按使用次数降序排序
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                // 将Map.Entry转换为响应对象 false true
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .toList();
        System.out.println("spaceTagAnalyzeResponses = " + spaceTagAnalyzeResponses);
        return spaceTagAnalyzeResponses;
    }

    /**
     * 空间图片大小分析
     *
     * @param spaceSizeAnalyzeRequest
     * @param loginUser
     * @return
     */
    @Override
    public List<SpaceSizeAnalyzeResponse> spaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 权限校验
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillSpaceAnalyzeRequest(spaceSizeAnalyzeRequest, queryWrapper);
        // 查询所有符合范围的图片大小
        queryWrapper.select("picSize");
        List<Long> picSizeList = pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .map(size -> ((Number) size).longValue())
                .toList();
        // 定义分段范围,注意使用有序Map
        Map<String, Long> sizeRange = new LinkedHashMap<>();
        sizeRange.put("<100KB", picSizeList.stream().filter(size -> size < 100 * 1024).count());
        sizeRange.put("100KB-500KB", picSizeList.stream().filter(size -> size >= 100 * 1024 && size < 500 * 1024).count());
        sizeRange.put("500KB-1MB", picSizeList.stream().filter(size -> size >= 500 * 1024 && size < 1024 * 1024).count());
        sizeRange.put(">1MB", picSizeList.stream().filter(size -> size >= 1024 * 1024).count());
        // 转换为响应对象
        return sizeRange.entrySet().stream()
                .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * 空间用户上传行为分析
     *
     * @param spaceUserAnalyzeRequest
     * @param loginUser
     * @return
     */
    @Override
    public List<SpaceUserAnalyzeResponse> spaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 权限校验
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillSpaceAnalyzeRequest(spaceUserAnalyzeRequest, queryWrapper);
        Long userId = spaceUserAnalyzeRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        // 分析维度: 每日,每周,每月,每年
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day":
                queryWrapper.select("DATE_FORMAT(createTime,'%Y-%m-%d') as period", "count(*) as count");
                break;
            case "week":
                queryWrapper.select("DATE_FORMAT(createTime,'%Y-%u') as period", "count(*) as count");
                break;
            case "month":
                queryWrapper.select("DATE_FORMAT(createTime,'%Y-%m') as period", "count(*) as count");
                break;
            case "year":
                queryWrapper.select("DATE_FORMAT(createTime,'%Y') as period", "count(*) as count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"不支持的时间维度");
        }
        // 根据时间维度分组并排序
        queryWrapper.groupBy("period").orderByAsc("period");
        // 查询结果
        List<Map<String, Object>> result = pictureService.getBaseMapper().selectMaps(queryWrapper);
        return result.stream()
                .map(map -> new SpaceUserAnalyzeResponse((String) map.get("period"), ((Number) map.get("count")).longValue()))
                .toList();
    }

    /**
     * 空间使用后排行分析
     * @param spaceRankAnalyzeRequest
     * @param loginUser
     * @return
     */
    @Override
    public List<Space> spaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 权限校验
        ThrowUtils.throwIf(!userService.isAdmin(loginUser),ErrorCode.NO_AUTH_ERROR,"只有管理员才有权限查看");
        // 构造查询条件
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","spaceName","userId","totalSize")
                .orderByDesc("totalSize")
                .last("limit " + spaceRankAnalyzeRequest.getTopN());
        // 查询结果
        return spaceService.list(queryWrapper);
    }


}





