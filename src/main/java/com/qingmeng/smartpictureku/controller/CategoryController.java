package com.qingmeng.smartpictureku.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingmeng.smartpictureku.annotation.AuthCheck;
import com.qingmeng.smartpictureku.common.BaseResponse;
import com.qingmeng.smartpictureku.common.DeleteRequest;
import com.qingmeng.smartpictureku.common.PageRequest;
import com.qingmeng.smartpictureku.common.ResultUtils;
import com.qingmeng.smartpictureku.constant.UserConstant;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.model.dto.category.CategoryAddRequest;
import com.qingmeng.smartpictureku.model.entity.Category;
import com.qingmeng.smartpictureku.model.vo.CategoryVO;
import com.qingmeng.smartpictureku.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * &#064;description:分类接口
 *
 * @author Wang
 * &#064;date: 2025-04-17
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    /**
     * 分页获取分类列表（管理员）
     * @param pageRequest 分页请求参数
     * @param type 分类类型（可选）
     * @return 分类列表（包含统计信息）
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<CategoryVO>> listCategoryVO(@RequestBody PageRequest pageRequest,
                                                         @RequestParam(required = false) Integer type) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        if (type != null) {
            queryWrapper.eq("categoryType", type);
        }

        Page<Category> categoryPage = categoryService.page(
                new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize()),
                queryWrapper
        );

        Page<CategoryVO> categoryVoPage = new Page<>(
                pageRequest.getCurrent(),
                pageRequest.getPageSize(),
                categoryPage.getTotal()
        );

        List<CategoryVO> categoryVOList = categoryService.listCategoryVO(categoryPage.getRecords());
        categoryVoPage.setRecords(categoryVOList);
        return ResultUtils.success(categoryVoPage);
    }

    /**
     * 获取指定类型的分类列表
     * @param type 分类类型（1-图片分类，2-帖子分类）
     * @return 分类名称列表
     */
    @GetMapping("/list/type/{type}")
    public BaseResponse<List<String>> listCategoryByType(@PathVariable Integer type) {
        return ResultUtils.success(categoryService.listCategoryByType(type));
    }

    /**
     * 添加新分类（管理员）
     * @param categoryAddRequest 分类添加请求对象
     * @return 添加结果
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> addCategory(@RequestBody CategoryAddRequest categoryAddRequest) {
        ThrowUtils.throwIf(categoryAddRequest == null, ErrorCode.PARAMS_ERROR);
        String categoryName = categoryAddRequest.getCategoryName();
        Integer type = categoryAddRequest.getType();
        return ResultUtils.success(categoryService.addCategory(categoryName, type));
    }

    /**
     * 删除分类（管理员）
     * @param categoryId 分类ID
     * @return 删除结果
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteCategory(@RequestBody DeleteRequest categoryId) {
        return ResultUtils.success(categoryService.removeById(categoryId));
    }

    /**
     * 搜索分类（管理员）
     * @param categoryName 分类名称关键词
     * @param type 分类类型（可选）
     * @return 匹配的分类列表
     */
    @PostMapping("/search")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<CategoryVO>> findCategory(@RequestParam String categoryName,
                                                       @RequestParam(required = false) Integer type) {
        ThrowUtils.throwIf(categoryName == null || categoryName.trim().isEmpty(),
                ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(categoryService.findCategory(categoryName, type));
    }


}
