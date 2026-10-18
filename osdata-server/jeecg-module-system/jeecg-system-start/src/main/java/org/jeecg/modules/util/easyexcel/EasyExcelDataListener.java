package org.jeecg.modules.util.easyexcel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * EasyExcel 读取类
 * @param <T> 读取的 excel 映射类
 * @param <T1> 实体类 getList 返回的数据类型
 */
// 有个很重要的点 DemoDataListener 不能被spring管理，要每次读取excel都要new,然后里面用到spring可以构造方法传进去
public class EasyExcelDataListener<T> extends AnalysisEventListener<T> {

    private List<T> list = new ArrayList<T>();

    public EasyExcelDataListener() {
    }

    /**
     * 这个每一条数据解析都会来调用
     *
     * @param data
     * @param context
     */
    @Override
    public void invoke(T data, AnalysisContext context) {
        list.add(data);
    }

    /**
     * 所有数据解析完成了 都会来调用
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
//        LOGGER.info("所有数据解析完成！");
    }

    /**
     * 获取数据列表
     * @return
     */
    public List<T> getList() {
        return list;
    }
}
