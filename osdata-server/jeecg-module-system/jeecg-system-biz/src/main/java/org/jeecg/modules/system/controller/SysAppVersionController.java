package org.jeecg.modules.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysAppVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.io.InputStream;

/**
* @Description: app系统配置
* @Author: jeecg-boot
* @Date:   2025-07-05
* @Version: V1.0
*/
@Tag(name="app系统配置")
@RestController
@RequestMapping("/sys/version")
@Slf4j
public class SysAppVersionController{

    @Autowired
    private RedisUtil redisUtil;
    /**
     * app3版本json文件路径
     */
    private final String JSON_PATH = "classpath:org/jeecg/modules/system/config/json/app3-version.json";

    /**
     * APP缓存前缀
     */
    private String APP3_VERSION = "app3:version";
    /**
     * app3版本信息
     * @return
     */
    @Operation(summary="app版本")
    @GetMapping(value = "/app3version")
    public Result<SysAppVersion> app3Version(@RequestParam(name="key", required = false)String appKey) throws Exception {
        Object appConfig = redisUtil.get(APP3_VERSION + appKey);
        if (oConvertUtils.isNotEmpty(appConfig)) {
            try {
                SysAppVersion sysAppVersion = (SysAppVersion)appConfig;
                if(oConvertUtils.isEmpty(sysAppVersion.getDownloadUrl())){
                    String jsonContent = readJson(JSON_PATH);
                    sysAppVersion = JSONObject.parseObject(jsonContent, SysAppVersion.class);
                    return Result.OK(sysAppVersion);
                }
                return Result.OK(sysAppVersion);
            } catch (Exception e) {
                log.error(e.toString(),e);
                return Result.error("app版本信息获取失败：" + e.getMessage());
            }
        }else{
            // 缓存中没有，从配置的json文件中获取
            try {
                String jsonContent = readJson(JSON_PATH);
                SysAppVersion sysAppVersion = JSONObject.parseObject(jsonContent, SysAppVersion.class);
                return Result.OK(sysAppVersion);
            } catch (Exception e) {
                log.error("从JSON文件读取app版本信息失败：{}", e);
            }
        }
        return Result.OK();
    }

    /**
     *   保存APP3
     *
     * @param sysAppVersion
     * @return
     */
    @RequiresRoles({"admin"})
    @Operation(summary="app系统配置-保存")
    @PostMapping(value = "/saveVersion")
    public Result<?> saveVersion(@RequestBody SysAppVersion sysAppVersion) {
        String id = sysAppVersion.getId();
        redisUtil.set(APP3_VERSION + id,sysAppVersion);
        return Result.OK();
    }

    /**
     * 读取json格式文件
     * @param jsonSrc
     * @return
     */
    private String readJson(String jsonSrc) {
        String json = "";
        try {
            //换个写法，解决springboot读取jar包中文件的问题
            InputStream stream = getClass().getClassLoader().getResourceAsStream(jsonSrc.replace("classpath:", ""));
            json = IOUtils.toString(stream,"UTF-8");
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
        return json;
    }
}
