package com.qingmeng.smartpictureku.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingmeng.smartpictureku.exception.ErrorCode;
import com.qingmeng.smartpictureku.exception.ThrowUtils;
import com.qingmeng.smartpictureku.model.dto.message.MessageAddRequest;
import com.qingmeng.smartpictureku.model.entity.Message;
import com.qingmeng.smartpictureku.model.vo.MessageVO;
import com.qingmeng.smartpictureku.service.MessageService;
import com.qingmeng.smartpictureku.mapper.MessageMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Wang
 * @description 针对表【message(留言板表)】的数据库操作Service实现
 * @createDate 2025-04-19 20:33:20
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
        implements MessageService {

    /**
     * 添加留言
     *
     * @param messageAddRequest 添加留言请求
     * @return 添加结果
     */
    @Override
    public Boolean addMessage(MessageAddRequest messageAddRequest) {
        ThrowUtils.throwIf(messageAddRequest == null, ErrorCode.PARAMS_ERROR);
        String content = messageAddRequest.getContent();
        String ip = messageAddRequest.getIp();
        Message message = new Message();
        message.setContent(content);
        message.setIp(ip);
        return this.save(message);
    }

    @Override
    public List<MessageVO> getTop500() {
        return this.baseMapper.getTop500();
    }
}




