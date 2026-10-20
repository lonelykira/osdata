package org.jeecg.modules.weo.service;

import org.jeecg.modules.weo.entity.WeoData;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 世界经济组织数据表
 * @Author: jeecg-boot
 * @Date:   2026-10-20
 * @Version: V1.0
 */
public interface IWeoDataService extends IService<WeoData> {

    /**
     * 导入WEO宽表Excel, 一行按年份列拆分为多条长表记录
     *
     * @param file excel文件
     */
    void importExcel(MultipartFile file);
}
