package com.qingmeng.smartpictureku.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingmeng.smartpictureku.model.entity.Message;
import com.qingmeng.smartpictureku.model.vo.MessageVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author Wang
* @description 针对表【message(留言板表)】的数据库操作Mapper
* @createDate 2025-04-19 20:33:20
* @Entity com.qingmeng.smartpictureku.model.entity.Message
*/
public interface MessageMapper extends BaseMapper<Message> {

    @Select("select id,content,createTime,ip from message order by id desc limit 500")
    List<MessageVO> getTop500();
}




