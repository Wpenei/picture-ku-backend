package com.qingmeng.smartpictureku.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qingmeng.smartpictureku.model.dto.message.MessageAddRequest;
import com.qingmeng.smartpictureku.model.entity.Message;
import com.qingmeng.smartpictureku.model.vo.MessageVO;

import java.util.List;

/**
* @author Wang
* @description 针对表【message(留言板表)】的数据库操作Service
* @createDate 2025-04-19 20:33:20
*/
public interface MessageService extends IService<Message> {

    /**
     * 添加留言
     */
    Boolean addMessage(MessageAddRequest messageAddRequest);

    List<MessageVO> getTop500();
}
