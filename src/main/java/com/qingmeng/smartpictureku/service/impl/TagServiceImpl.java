package com.qingmeng.smartpictureku.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingmeng.smartpictureku.exception.BusinessException;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.mapper.TagMapper;
import com.qingmeng.smartpictureku.model.entity.Tag;
import com.qingmeng.smartpictureku.model.vo.TagVO;
import com.qingmeng.smartpictureku.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * &#064;description:针对表【tag(标签)】的数据库操作Service实现
 *
 * @author Wang
 * &#064;date: 2025-04-17 12:43:17
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService{

    /**
     * 获取所有标签名列表
     * @return 标签名列表
     */
    @Override
    public List<String> listTag() {
        // 获取所有标签
        List<Tag> tags = this.list();
        // 提取标签名
        return tags.stream().map(Tag::getTagName).toList();
        // todo 优化搜索，使用只查询标签名的SQL语句
    }

    /**
     * 将Tag对象转换为TagVO对象
     * @param tag Tag对象
     * @return TagVO对象
     */
    @Override
    public TagVO getTagVO(Tag tag) {
        if (tag == null){
            return null;
        }
        TagVO tagVO = new TagVO();
        BeanUtil.copyProperties(tag,tagVO);
        return tagVO;
    }

    /**
     * 将Tag对象列表转换为TagVO对象列表，并按页返回
     * @param records Tag对象列表
     * @return TagVO对象列表
     */
    @Override
    public List<TagVO> listTagVoByPage(List<Tag> records) {
        // 1.校验参数
        if(CollUtil.isEmpty(records)){
            return List.of();
        }
        // 2.转换对象
        return records.stream().map(this::getTagVO).toList();
    }

    /**
     * 添加新的标签
     * @param tagName 标签名称
     * @return 添加成功返回true，失败返回false
     */
    @Override
    public Boolean addTag(String tagName) {
        // 创建查询条件包装器
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        // 使用like进行模糊查询，匹配标签名称包含输入的tagName的记录
        queryWrapper.eq("tagName", tagName);
        // 执行查询
        List<Tag> tag = this.list(queryWrapper);
        if (tag != null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标签已存在");
        }
        Tag newTag = new Tag();
        newTag.setTagName(tagName);
        return this.save(newTag);
    }

    /**
     * 根据标签名称搜索标签
     * @param tagName 标签名称
     * @return 标签VO对象列表
     */
    @Override
    public List<TagVO> searchTag(String tagName) {
        // 创建查询条件包装器
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        // 使用like进行模糊查询，匹配标签名称包含输入的tagName的记录
        queryWrapper.like("tagName", tagName);
        // 执行查询
        List<Tag> tags = this.list(queryWrapper);
        return this.listTagVoByPage(tags);
    }
}




