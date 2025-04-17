package com.qingmeng.smartpictureku.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingmeng.smartpictureku.annotation.AuthCheck;
import com.qingmeng.smartpictureku.common.BaseResponse;
import com.qingmeng.smartpictureku.common.PageRequest;
import com.qingmeng.smartpictureku.common.ResultUtils;
import com.qingmeng.smartpictureku.constant.UserConstant;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.model.entity.Tag;
import com.qingmeng.smartpictureku.model.vo.TagVO;
import com.qingmeng.smartpictureku.service.TagService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * &#064;description:标签接口
 *
 * @author Wang
 * &#064;date: 2025/3/5
 */
@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    private TagService tagService;

    /**
     * 获取所有标签
     * @param pageRequest 分页参数
     * @return 标签列表
     */
    @PostMapping("list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<TagVO>> listTagVOByPage(@RequestBody PageRequest pageRequest){
        long current = pageRequest.getCurrent();
        long pageSize = pageRequest.getPageSize();
        Page<Tag> tagPage = tagService.page(new Page<>(current, pageSize));
        Page<TagVO> tagVoPage = new Page<>(current, pageSize,tagPage.getTotal());
        List<TagVO> tagVOList = tagService.listTagVoByPage(tagPage.getRecords());
        tagVoPage.setRecords(tagVOList);
        return ResultUtils.success(tagVoPage);
    }

    /**
     * 添加标签
     * @param tagName 标签名称
     * @return 添加成功返回true，失败返回false
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> addTag(@RequestParam String tagName){
        ThrowUtils.throwIf(tagName == null || tagName.length() == 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(tagService.addTag(tagName));
    }

    /**
     * 删除标签
     * @param id 标签id
     * @return 删除成功返回true，失败返回false
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteTag(@RequestParam Long id){
        ThrowUtils.throwIf(id == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(tagService.removeById(id));
    }

    /**
     * 查找标签
     * @param tagName 标签名称
     * @return 标签列表
     */
    @PostMapping("/search")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<TagVO>> searchTag(@RequestParam String tagName){
        ThrowUtils.throwIf(tagName == null || tagName.length() == 0, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(tagService.searchTag(tagName));
    }

}
