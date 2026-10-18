package org.jeecg.modules.base.query;

import lombok.Data;

@Data
public class BaseQuery {
    /**
     * 页码
     */
    private Integer pageNo;

    /**
     * 数量
     */
    private Integer pageSize;

    public Integer getPageSize() {
        if(this.pageSize > 50) {
            this.pageSize = 50;
        }
        return this.pageSize;
    }
}
