package org.jeecg.common.system.query;

import java.lang.annotation.*;

/**
 * 查询条件忽略注解
 * <p>标记此注解的字段将被 QueryGenerator 跳过，不参与查询条件构建。
 * 适用于密码、盐值等敏感字段，防止通过请求参数进行模糊查询探测。</p>
 *
 * @see QueryGenerator#initQueryWrapper
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueryConditionIgnore {
}
