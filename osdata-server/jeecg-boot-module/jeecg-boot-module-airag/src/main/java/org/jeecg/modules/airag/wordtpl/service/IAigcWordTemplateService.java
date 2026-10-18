package org.jeecg.modules.airag.wordtpl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.airag.wordtpl.dto.WordTplGenDTO;
import org.jeecg.modules.airag.wordtpl.entity.AigcWordTemplate;

import java.io.ByteArrayOutputStream;

/**aigc_word_template
 * @Description: word模版管理
 * @Author: jeecg-boot
 * @Date:   2025-07-04
 * @Version: V1.0
 */
public interface IAigcWordTemplateService extends IService<AigcWordTemplate> {

    /**
     * 通过模版生成word文档
     *
     * @param wordTplGenDTO
     * @return
     * @author chenrui
     * @date 2025/7/10 14:40
     */
    void generateWordFromTpl(WordTplGenDTO wordTplGenDTO, ByteArrayOutputStream wordOutputStream);
}
