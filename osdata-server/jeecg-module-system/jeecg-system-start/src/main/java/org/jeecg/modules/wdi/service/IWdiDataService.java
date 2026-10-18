package org.jeecg.modules.wdi.service;

import org.jeecg.modules.wdi.entity.WdiData;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: 世行WDI面板数据长表
 * @Author: jeecg-boot
 * @Date:   2026-10-12
 * @Version: V1.0
 */
public interface IWdiDataService extends IService<WdiData> {
    /**
     * import
     * @param file
     */
    void importExcel(MultipartFile file);
}
