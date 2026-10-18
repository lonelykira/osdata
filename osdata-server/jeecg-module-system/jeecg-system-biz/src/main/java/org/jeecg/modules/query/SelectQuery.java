package org.jeecg.modules.query;

import java.util.List;

public class SelectQuery {
    /**
     * 页码
     */
    private Integer pageNo = 1;

    /**
     * 记录数
     */
    private Integer pageSize = 20;

    private String keyword;

    private List<String> ids;

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

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
