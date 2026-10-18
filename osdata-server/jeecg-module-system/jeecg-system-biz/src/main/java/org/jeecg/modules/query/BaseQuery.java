package org.jeecg.modules.query;

import lombok.Data;

@Data
public class BaseQuery {
    /**
     * 页码
     */
    private Integer pageNo = 1;

    /**
     * 记录数
     */
    private Integer pageSize = 10;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
