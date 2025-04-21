package com.qingmeng.smartpictureku.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingmeng.smartpictureku.model.entity.SpaceUser;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author Wang
* @description 针对表【space_user(空间用户关联表)】的数据库操作Mapper
* @createDate 2025-03-15 15:08:57
* @Entity com.qingmeng.smartpictureku.model.entity.SpaceUser
*/
public interface SpaceUserMapper extends BaseMapper<SpaceUser> {

    // 多表查询，根据用户id查找到用户加入的团队空间，根据空间id，在空间表中查询创建人id，只获取创建人id与用户id不同空间用户信息
    @Select("select * from space_user where userId = #{userId} and spaceId in (select id from space where userId = #{userId} and spaceType = 1)")
    List<SpaceUser> selectSpaceUserByUserIdCreate(Long userId);

    // 多表查询，根据用户id查找到用户加入的团队空间，根据空间id，在空间表中查询创建人id，只获取创建人id与用户id不同空间用户信息
    @Select("select * from space_user where userId = #{userId} and spaceId not in (select id from space where userId = #{userId})")
    List<SpaceUser> selectSpaceUserByUserIdJoin(Long userId);



}




