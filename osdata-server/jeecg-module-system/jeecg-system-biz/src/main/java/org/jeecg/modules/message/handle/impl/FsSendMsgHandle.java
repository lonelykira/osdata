package org.jeecg.modules.message.handle.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.modules.message.handle.ISendMsgHandle;
import org.jeecg.modules.system.service.impl.ThirdAppFeishuServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 发飞书消息模板
 *
 * @author jeecg-boot
 */
@Slf4j
@Component("fsSendMsgHandle")
public class FsSendMsgHandle implements ISendMsgHandle {

    @Autowired
    private ThirdAppFeishuServiceImpl feishuService;

    @Override
    public void sendMsg(String esReceiver, String esTitle, String esContent) {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setToUser(esReceiver);
        messageDTO.setTitle(esTitle);
        messageDTO.setContent(esContent);
        messageDTO.setToAll(false);
        sendMessage(messageDTO);
    }

    @Override
    public void sendMessage(MessageDTO messageDTO) {
        feishuService.sendMessage(messageDTO, true);
    }

}
