package com.qingmeng.smartpictureku.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qingmeng.smartpictureku.model.entity.Category;
import com.qingmeng.smartpictureku.model.vo.CategoryVO;

import java.util.List;

/**
* @author Wang
* @description 针对表【category(分类)】的数据库操作Service
* @createDate 2025-04-17 13:39:23
*/
public interface CategoryService extends IService<Category> {
    /**
     * 获取分类列表，不传类型默认为图片分类
     */
    default List<String> listCategory() {
        // 默认图片分类
        return listCategoryByType(0);
    }

    /**
     * 获取指定类型的分类列表
     * @param type 分类类型：0-图片分类 1-帖子分类
     */
    List<String> listCategoryByType(Integer type);

    List<CategoryVO> listCategoryVO(List<Category> records);

    CategoryVO getCategoryVO(Category category);

    /**
     * 根据名称查找分类，不传类型默认为图片分类
     */
    default List<CategoryVO> findCategory(String categoryName) {
        return findCategory(categoryName, 0);  // 默认图片分类
    }

    /**
     * 根据名称和类型查找分类
     */
    List<CategoryVO> findCategory(String categoryName, Integer type);

    /**
     * 添加分类，不传类型默认为图片分类
     */
    default boolean addCategory(String categoryName) {
        return addCategory(categoryName, 0);  // 默认图片分类
    }

    /**
     * 添加指定类型的分类
     */
    boolean addCategory(String categoryName, Integer type);
}
