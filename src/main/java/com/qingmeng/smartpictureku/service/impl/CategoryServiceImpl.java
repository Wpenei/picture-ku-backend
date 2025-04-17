package com.qingmeng.smartpictureku.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.mapper.CategoryMapper;
import com.qingmeng.smartpictureku.model.entity.Category;
import com.qingmeng.smartpictureku.model.vo.CategoryVO;
import com.qingmeng.smartpictureku.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Wang
 * @description 针对表【category(分类)】的数据库操作Service实现
 * @createDate 2025-04-17 13:39:23
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {
    /**
     * 获取分类名称
     *
     * @param type 分类类型：0-图片分类 1-帖子分类
     * @return
     */
    @Override
    public List<String> listCategoryByType(Integer type) {
        //
        List<Category> list = this.list();
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return list.stream().map(Category::getCategoryName).collect(Collectors.toList());
        // todo 优化搜索，使用只查询分类名的SQL语句
        //return this.baseMapper.listCategoryByType(type);
    }

    @Override
    public CategoryVO getCategoryVO(Category category) {
        if (category == null) {
            return null;
        }
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);
        return categoryVO;
    }

    @Override
    public List<CategoryVO> listCategoryVO(List<Category> records) {
        if (CollUtil.isEmpty(records)) {
            return null;
        }
        return records.stream().map(this::getCategoryVO).collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> findCategory(String categoryName, Integer type) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("categoryName", categoryName);
        if (type != null) {
            queryWrapper.eq("categoryType", type);
        }
        List<Category> categoryList = this.list(queryWrapper);
        return listCategoryVO(categoryList);
    }

    @Override
    public boolean addCategory(String categoryName, Integer type) {
        // 判断分类名是否已存在
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("categoryName", categoryName);
        if (type != null) {
            queryWrapper.eq("categoryType", type);
        }
        List<Category> categoryList = this.baseMapper.selectList(queryWrapper);
        if (CollUtil.isNotEmpty(categoryList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类名已存在");
        }
        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setCategoryType(type);
        return this.save(category);
    }
}




