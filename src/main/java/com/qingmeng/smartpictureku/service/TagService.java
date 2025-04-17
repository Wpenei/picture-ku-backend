package com.qingmeng.smartpictureku.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qingmeng.smartpictureku.model.entity.Tag;
import com.qingmeng.smartpictureku.model.vo.TagVO;

import java.util.List;

/**
 * &#064;description:针对表【tag(标签)】的数据库操作Service实现
 *
 * @author Wang
 * &#064;date: 2025-04-17 12:43:17
 */
public interface TagService extends IService<Tag> {

    /**
     * 获取所有标签的名称列表
     * @return 标签名称列表
     */
    List<String> listTag();

    /**
     * 将Tag对象转换为TagVO对象
     * @param tag Tag对象
     * @return TagVO对象
     */
    TagVO getTagVO(Tag tag);

    /**
     * 将Tag对象列表转换为TagVO对象列表，并按页返回
     * @param records Tag对象列表
     * @return TagVO对象列表
     */
    List<TagVO> listTagVoByPage(List<Tag> records);

    /**
     * 添加新的标签
     * @param tagName 标签名称
     * @return 添加成功返回true，失败返回false
     */
    Boolean addTag(String tagName);

    /**
     * 根据标签ID删除标签
     * @param id 标签ID
     * @return 删除成功返回true，失败返回false
     */
    Boolean deleteTag(Long id);

    /**
     * 根据标签名称搜索标签
     * @param tagName 标签名称
     * @return 标签VO对象列表
     */
    List<TagVO> searchTag(String tagName);
}
