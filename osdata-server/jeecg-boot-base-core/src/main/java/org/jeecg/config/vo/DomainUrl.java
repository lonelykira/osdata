package org.jeecg.config.vo;

import lombok.Data;

/**
 * @Author taoYan
 * @Date 2022/7/5 21:16
 **/
@Data
public class DomainUrl {

    private String pc;

    private String app;

    /**
     * 后端自代理 baseUrl：相对路径 originUrl 转发时使用，
     * 解决 Docker / K8s NodePort / 反向代理等入站端口与监听端口不一致问题。
     */
    private String back;
}
