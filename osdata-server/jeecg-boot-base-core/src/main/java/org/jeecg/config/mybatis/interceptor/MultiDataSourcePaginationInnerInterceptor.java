package org.jeecg.config.mybatis.interceptor;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.IDialect;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 支持动态多数据源的分页拦截器
 *
 * <p>原生 {@link PaginationInnerInterceptor} 一旦用固定 {@link DbType} 构造，
 * {@code findIDialect()} 会缓存该方言并对所有数据源生效。当主库与 Online 表单/报表
 * 使用的从数据源类型不一致时（例如主库 SQL Server、从库 MySQL），会用主库方言分页，
 * 生成对方数据库无法识别的分页 SQL 而报错。</p>
 *
 * <p>本子类做两件事，均按当前连接实时探测数据库类型（{@link JdbcUtils#getDbType(Executor)}
 * 内部按 URL 缓存，开销极小），从而正确适配多数据源：</p>
 * <ol>
 *   <li>{@link #findIDialect(Executor)}：让不同类型的数据源各自使用正确的分页方言，
 *       其中 SQL Server 统一映射为 {@link DbType#SQL_SERVER2005} 方言，以保留
 *       issues/8666 对旧版 SQL Server 分页的兼容处理；</li>
 *   <li>{@link #beforeQuery}：仅当探测到 SQL Server 时，对 ORDER BY 去重
 *       （保留首次出现、维持排序优先级），解决 QueryGenerator + 手动 orderBy 叠加导致
 *       重复列 error 169 的问题；其它数据库不做处理，避免无谓的 SQL 解析开销。</li>
 * </ol>
 *
 * @author scott
 * @date 2026-07-09
 */
@Slf4j
public class MultiDataSourcePaginationInnerInterceptor extends PaginationInnerInterceptor {

    @Override
    protected IDialect findIDialect(Executor executor) {
        if (isSqlServer(executor)) {
            // SQL Server 统一走 2005 方言（ROW_NUMBER 分页），兼容旧版 SQL Server
            return DialectFactory.getDialect(DbType.SQL_SERVER2005);
        }
        return DialectFactory.getDialect(JdbcUtils.getDbType(executor));
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        // 仅 SQL Server 需要 ORDER BY 去重（error 169），其它库跳过以免无谓的 SQL 解析开销
        if (isSqlServer(executor)) {
            dedupOrderBy(boundSql);
        }
        super.beforeQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql);
    }

    /**
     * 按当前连接判断是否为 SQL Server
     */
    private boolean isSqlServer(Executor executor) {
        DbType dbType = JdbcUtils.getDbType(executor);
        return dbType == DbType.SQL_SERVER || dbType == DbType.SQL_SERVER2005;
    }

    /**
     * SQL Server ORDER BY 去重：移除 ORDER BY 中重复的列（保留首次出现，维持排序优先级）。
     * SQL Server 不允许 ORDER BY 中出现重复列（error 169），而项目中有多处在
     * QueryGenerator.initQueryWrapper() 之后又手动调了 orderByDesc/orderByAsc。
     */
    private void dedupOrderBy(BoundSql boundSql) {
        String originalSql = boundSql.getSql();
        if (originalSql == null || originalSql.isEmpty()) {
            return;
        }
        // 快速跳过非 SELECT 语句
        String upper = originalSql.trim().toUpperCase();
        if (!upper.startsWith("SELECT") && !upper.startsWith("(SELECT")) {
            return;
        }
        try {
            Statement stmt = CCJSqlParserUtil.parse(originalSql);
            List<OrderByElement> orderByElements = null;
            if (stmt instanceof PlainSelect) {
                orderByElements = ((PlainSelect) stmt).getOrderByElements();
            } else if (stmt instanceof SetOperationList) {
                orderByElements = ((SetOperationList) stmt).getOrderByElements();
            }
            if (orderByElements == null || orderByElements.size() <= 1) {
                return;
            }

            // 去重：保留首次出现，维持顺序
            List<OrderByElement> deduped = new ArrayList<>(orderByElements.size());
            LinkedHashSet<String> seen = new LinkedHashSet<>();
            for (OrderByElement element : orderByElements) {
                // toString() 包含表达式 + ASC/DESC + NULLS FIRST/LAST，完整作为去重 key
                if (seen.add(element.toString())) {
                    deduped.add(element);
                }
            }
            if (deduped.size() == orderByElements.size()) {
                return;
            }

            // 回写去重后的 ORDER BY
            if (stmt instanceof PlainSelect) {
                ((PlainSelect) stmt).setOrderByElements(deduped);
            } else {
                ((SetOperationList) stmt).setOrderByElements(deduped);
            }
            PluginUtils.mpBoundSql(boundSql).sql(stmt.toString());
            log.debug("SQL Server ORDER BY 去重: {} 个重复列已移除", orderByElements.size() - deduped.size());
        } catch (Exception e) {
            // 解析失败时保持原始 SQL，不中断查询
            log.warn("SQL Server ORDER BY 去重解析失败，使用原始 SQL: {}", e.getMessage());
        }
    }
}
