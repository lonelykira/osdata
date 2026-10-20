package org.jeecg.modules.api.weo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: WEO国家分页检索入参
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
@Data
@Schema(description = "WEO国家分页检索入参")
public class CountryPageDTO {

    /**页码(默认1)*/
    @Schema(description = "页码(默认1)")
    private Integer pageNo = 1;

    /**每页条数(默认10, 最大100)*/
    @Schema(description = "每页条数(默认10, 最大100)")
    private Integer pageSize = 10;

    /**关键词(模糊匹配国家中英文名称及国家代码)*/
    @Schema(description = "关键词(模糊匹配国家中英文名称及国家代码)")
    private String keyword;

    /**类型筛选: 0=单个国家 1=聚合经济体/区域分组(空查全部)*/
    @Schema(description = "类型筛选: 0=单个国家 1=聚合经济体/区域分组(空查全部)")
    private Integer isAggregate;
}
