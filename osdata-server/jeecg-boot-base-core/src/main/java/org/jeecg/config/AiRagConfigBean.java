package org.jeecg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ai配置类，通用的配置可以放到这里面
 *
 * @Author: wangshuai
 * @Date: 2025/12/17 14:00
 */
@Data
@Component
@ConfigurationProperties(prefix = AiRagConfigBean.PREFIX)
public class AiRagConfigBean {
    public static final String PREFIX = "jeecg.airag";
    
    /**
     * 敏感节点
     * stdio mpc命令行功能开启，sql：AI流程SQL节点开启
     */
    private String allowSensitiveNodes = "";

    //update-begin---author:wangshuai ---date:2026-04-15  for：Brave Search配置迁移到AiRagConfigBean，去掉enabled字段，apiKey为空即不启用-----------
    /**
     * Brave Search 联网检索配置
     */
    private BraveSearchConfig braveSearch = new BraveSearchConfig();

    @Data
    public static class BraveSearchConfig {
        /** Brave Search API Key；为空时联网检索不生效 */
        private String apiKey;
        /** API 端点，默认官方地址 */
        private String endpoint = "https://api.search.brave.com/res/v1/web/search";
        /** 默认返回结果条数，最大 20 */
        private Integer count = 10;
        /** 请求超时秒数 */
        private Integer timeout = 15;
        /**
         * 搜索结果缓存时长（分钟）。
         * 大于 0 时开启缓存，相同参数的查询直接返回缓存结果，不重复调用 API。
         * 设为 0 或不配置则关闭缓存。
         */
        private Integer cacheExpireMinutes = 60;
    }
    //update-end---author:wangshuai ---date:2026-04-15  for：Brave Search配置迁移到AiRagConfigBean，去掉enabled字段，apiKey为空即不启用-----------
}
