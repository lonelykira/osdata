package org.jeecg.config.init;

import org.apache.catalina.Context;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @Description: TomcatFactoryConfig
 * @author: scott
 * @date: 2021年01月25日 11:40
 */
@Configuration
public class TomcatFactoryConfig {
    /**
     * tomcat-embed-jasper引用后提示jar找不到的问题
     */
    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                ((StandardJarScanner) context.getJarScanner()).setScanManifest(false);
            }
        };
        factory.addConnectorCustomizers(connector -> {
            connector.setProperty("relaxedPathChars", "[]{}");
            connector.setProperty("relaxedQueryChars", "[]{}");
        });
        //update-begin---author:scott ---date:20260710  for：【issues】升级Tomcat11后work目录生成在项目目录问题-----------
        // 自定义Bean覆盖了SpringBoot自动配置，yml里的basedir不生效，Tomcat11默认落到./work，需手动指定系统临时目录
        try {
            File tomcatTmpDir = Files.createTempDirectory("tomcat.jeecg.").toFile();
            tomcatTmpDir.deleteOnExit();
            factory.setBaseDirectory(tomcatTmpDir);
        } catch (IOException e) {
            // ignore, fallback to tomcat default
        }
        //update-end---author:scott ---date:20260710  for：【issues】升级Tomcat11后work目录生成在项目目录问题-----------
        return factory;
    }
}
