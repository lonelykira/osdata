package org.jeecg.modules.util.easyexcel;

import com.alibaba.excel.EasyExcel;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EasyExcelUtil {

    /**
     * easyEasy 读取excel工具
     * @param entityClazz 返回的实体类型
     * @param headerNum 标题行号
     * @param file excel 文件
     * @param <T>
     * @return
     */
    public static <T> List<T> easyExcelRead(MultipartFile file, Class<T> entityClazz, Integer headerNum){
        List<T> list = new ArrayList<>();
        EasyExcelDataListener<T> listener = new EasyExcelDataListener<>();
        try {
            EasyExcel.read(file.getInputStream(), entityClazz, listener).sheet().headRowNumber(headerNum).doRead();
            list = listener.getList();
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 出库单excel构造
     * @param title
     * @return
     */
    public static List<List<String>> head1(String title,String lebal) {
        List<List<String>> list = new ArrayList<List<String>>();
        for (int i=0 ; i<=10;i++){
            List<String> row = Lists.newArrayList();
            row.add(title);
            row.add(title);
            switch (i){
                case 0:
                    row.add(lebal+"单号:");
                    row.add(lebal+"日期:");
                    row.add("序号");
                    break;
                case 1:
                    row.add("		");
                    row.add("			");
                    row.add("产品编码");
                    break;
                case 2:
                    row.add("		");
                    row.add("			");
                    row.add("产品名称");
                    break;
                case 3:
                    row.add("		");
                    row.add("			");
                    row.add("规格型号");
                    break;
                case 4:
                    row.add("部门审核人:");
                    row.add(lebal+"人员:");
                    row.add("计量单位");
                    break;
                case 5:
                    row.add(" ");
                    row.add("   ");
                    row.add(lebal+"时间");
                    break;
                case 6:
                    row.add("		");
                    row.add("			");
                    row.add(lebal+"数量");
                    break;
                case 7:
                    row.add("			");
                    row.add("仓库");
                    row.add("单价（元）");
                    break;
                case 8:
                    row.add("			");
                    row.add("广西兴桂船运有限公司");
                    row.add("金额（元）");
                    break;
                case 9:
                    row.add("			");
                    row.add("广西兴桂船运有限公司");
                    row.add("存货类别");
                    break;
                case 10:
                    row.add("			");
                    row.add("广西兴桂船运有限公司");
                    row.add("船舶名称");
                    break;
                default:
                    row.add("		");
                    row.add("			");
                    break;
            }
            list.add(row);
        }
        return list;

    }
}
