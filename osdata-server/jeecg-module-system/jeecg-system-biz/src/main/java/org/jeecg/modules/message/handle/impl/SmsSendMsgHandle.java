package org.jeecg.modules.message.handle.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.constant.enums.DySmsEnum;
import org.jeecg.common.util.DySmsHelper;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.message.handle.ISendMsgHandle;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 短信发送处理
 * @author: jeecg-boot
 */
@Slf4j
@Component("smsSendMsgHandle")
public class SmsSendMsgHandle implements ISendMsgHandle {

    @Autowired
    private SysUserMapper sysUserMapper;
    // 流程短信模版code
    private final String BPM_SMS_TEMPLATE_CODE = "bpm_sms";

    @Override
    public void sendMsg(String esReceiver, String esTitle, String esContent) {
        log.info("发短信，接收人: {}，标题: {}，内容: {}", esReceiver, esTitle, esContent);
    }

    @Override
    public void sendMessage(MessageDTO messageDTO) {
        String toUser = messageDTO.getToUser();
        if (oConvertUtils.isEmpty(toUser)) {
            log.error("短信发送失败：接收人为空");
            return;
        }

        // 根据模板编码获取阿里云短信模板配置
        String templateCode = messageDTO.getTemplateCode();
        DySmsEnum dySmsEnum = null;
        if (dySmsEnum == null) {
            log.warn("未找到精确匹配的短信模板，templateCode: {}");
            return;
        }
        // 解析短信模板参数（消息内容经过FreeMarker解析后应为JSON格式的模板参数）
        JSONObject templateParamJson = new JSONObject();
        try {
            String bpmTitle = messageDTO.getTitle();
            if (oConvertUtils.isEmpty(bpmTitle)) {
                log.error("短信发送失败：消息标题为空");
                return;
            }
            templateParamJson.put("bpmTitle", bpmTitle);
        } catch (Exception e) {
            log.error("短信发送失败：消息内容不是有效的JSON格式，content: {}", messageDTO.getContent(), e);
            return;
        }

        // 查询接收用户的手机号
        String[] usernames = toUser.split(",");
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getUsername, usernames)
                .isNotNull(SysUser::getPhone)
                .ne(SysUser::getPhone, "");
        List<SysUser> users = sysUserMapper.selectList(query);

        if (users == null || users.isEmpty()) {
            log.warn("短信发送失败：未找到有效的用户手机号，接收人: {}", toUser);
            return;
        }

        // 逐个发送短信
        int successCount = 0;
        int failCount = 0;
        for (SysUser user : users) {
            String phone = user.getPhone();
            if (oConvertUtils.isEmpty(phone)) {
                log.warn("用户 {} 未设置手机号，跳过短信发送", user.getUsername());
                failCount++;
                continue;
            }
            try {
                templateParamJson.put("realname", oConvertUtils.getString(user.getRealname(),user.getUsername()));
                boolean success = DySmsHelper.sendSms(phone, templateParamJson, dySmsEnum);
                if (success) {
                    log.info("短信发送成功，接收人: {}，手机号: {}", user.getUsername(), phone);
                    successCount++;
                } else {
                    log.error("短信发送失败（API返回失败），接收人: {}，手机号: {}", user.getUsername(), phone);
                    failCount++;
                }
            } catch (Exception e) {
                log.error("短信发送异常，接收人: {}，手机号: {}", user.getUsername(), phone, e);
                failCount++;
            }
        }
        log.info("短信发送完成，成功: {}，失败: {}，总数: {}", successCount, failCount, users.size());
    }
}
