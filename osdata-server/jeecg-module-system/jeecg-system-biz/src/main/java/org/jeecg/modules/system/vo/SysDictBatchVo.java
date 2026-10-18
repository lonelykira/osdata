package org.jeecg.modules.system.vo;

import lombok.Data;
import org.jeecg.modules.system.entity.SysDictItem;

import java.util.List;

/**
 * @Description: 批量字典VO
 * @author: zzl
 */
@Data
public class SysDictBatchVo {

    /**
     * 字典列表
     */
    private List<SysDictPage> dictList;

}
