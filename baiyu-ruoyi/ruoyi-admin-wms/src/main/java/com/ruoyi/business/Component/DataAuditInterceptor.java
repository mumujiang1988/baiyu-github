package com.ruoyi.business.Component;

import cn.hutool.json.JSONObject;
import com.ruoyi.business.entity.Bymaterial;
import com.ruoyi.common.core.utils.ServletUtils;
import com.ruoyi.common.satoken.utils.LoginHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 数据修改审计拦截器
 * 支持多表审计，每一次 UPDATE 只写一条日志，所有字段差异存入 diff_json
 */
@Slf4j
@Intercepts({
    @Signature(type = Executor.class, method = "update",
            args = {MappedStatement.class, Object.class})
})
@Component
public class DataAuditInterceptor implements Interceptor {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 配置需要审计的表及其主键字段
    private static final Map<String, String> AUDIT_TABLES = new HashMap<>();
    static {
        AUDIT_TABLES.put("by_material", "id");           // 物料表
        AUDIT_TABLES.put("customer", "id");              // 客户表
        AUDIT_TABLES.put("supplier", "id");              // 供应商表
        AUDIT_TABLES.put("purchase_price", "id");        // 采购价目表
        AUDIT_TABLES.put("inquiry_order", "fid");        // 询价单表
        // 后续需要审计的表在此添加即可
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        // MyBatis 执行对象
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object param = invocation.getArgs()[1];

        // 只处理 UPDATE 语句
        if (ms.getSqlCommandType() != SqlCommandType.UPDATE) {
            return invocation.proceed();
        }

        // 获取原始 SQL
        BoundSql boundSql = ms.getBoundSql(param);
        String sql = boundSql.getSql().toLowerCase();


        String targetTable = null;
        String primaryKey = null;
        for (Map.Entry<String, String> entry : AUDIT_TABLES.entrySet()) {
            if (sql.contains(entry.getKey())) {
                targetTable = entry.getKey();
                primaryKey = entry.getValue();
                break;
            }
        }

        // 不在审计范围内，直接放行
        if (targetTable == null) {
            return invocation.proceed();
        }

        // 从参数中提取主键值
        Object primaryKeyValue = extractPrimaryKey(param, primaryKey);
        String queryField = primaryKey; // 查询使用的字段

        // 如果提取不到主键，尝试使用 number 字段（特别处理 by_material 表）
        if (primaryKeyValue == null && "by_material".equals(targetTable)) {
            primaryKeyValue = extractPrimaryKey(param, "number");
            if (primaryKeyValue != null) {
                queryField = "number"; // 使用 number 字段查询
                log.debug("未找到 id，使用 number 字段: {}", primaryKeyValue);
            }
        }

        if (primaryKeyValue == null) {
            return invocation.proceed();
        }

        // 更新前查询旧数据
        Map<String, Object> oldRow = null;
        try {
            String querySql = String.format("SELECT * FROM %s WHERE %s = ?", targetTable, queryField);
            oldRow = jdbcTemplate.queryForMap(querySql, primaryKeyValue);
        } catch (Exception e) {
            log.warn("查询旧数据失败，表：{}，查询字段：{}，值：{}", targetTable, queryField, primaryKeyValue);
            return invocation.proceed();
        }

        Object result = invocation.proceed();

        // 更新后查询新数据
        Map<String, Object> newRow = null;
        try {
            // 使用相同的连接查询新数据，避免连接池问题
            String querySql = String.format("SELECT * FROM %s WHERE %s = ?", targetTable, queryField);
            newRow = jdbcTemplate.queryForMap(querySql, primaryKeyValue);
        } catch (Exception e) {
            log.warn("查询新数据失败，表：{}，查询字段：{}，值：{}", targetTable, queryField, primaryKeyValue);
            return result;
        }


        if (oldRow != null && newRow != null) {
            saveDiff(targetTable, oldRow, newRow, queryField);
        }

        return result;
    }

    /**
     * 从参数中提取主键值（支持多种参数类型）
     */
    private Object extractPrimaryKey(Object param, String primaryKey) {
        try {

            if (param instanceof Map) {
                Map<?, ?> paramMap = (Map<?, ?>) param;


                if (paramMap.containsKey(primaryKey)) {
                    return paramMap.get(primaryKey);
                }


                Object param1 = paramMap.get("param1");
                if (param1 != null) {
                    return extractFromObject(param1, primaryKey);
                }


                Object et = paramMap.get("et");
                if (et != null) {
                    return extractFromObject(et, primaryKey);
                }
            } else {

                return extractFromObject(param, primaryKey);
            }
        } catch (Exception e) {
            log.warn("提取主键值失败：{}", e.getMessage());
        }
        return null;
    }

    /**
     * 从对象中反射获取字段值
     */
    private Object extractFromObject(Object obj, String fieldName) {
        try {
            // 尝试通过 getter 方法获取
            String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            return obj.getClass().getMethod(methodName).invoke(obj);
        } catch (Exception e) {
            // 忽略异常，返回 null
        }
        return null;
    }

    /**
     * 生成 JSON 差异并保存日志
     */
    private void saveDiff(String tableName, Map<String, Object> oldRow, Map<String, Object> newRow, String primaryKey) {

        JSONObject diff = new JSONObject();

        // 定义不需要记录的字段（时间字段、系统自动维护字段等）
        Set<String> excludeFields = new HashSet<>();
        // 多种格式的时间字段都要排除
        excludeFields.add("modified");             // 修改时间（简写）
        excludeFields.add("modification_time");
        excludeFields.add("creatorTime");          // 创建时间（驼峰）

        oldRow.forEach((field, oldVal) -> {

            // 跳过不需要记录的字段（不区分大小写）
            if (excludeFields.stream().anyMatch(f -> f.equalsIgnoreCase(field))) {
                return;
            }

            Object newVal = newRow.get(field);

            // 值没变，不记录
            if (Objects.equals(oldVal, newVal)) {
                return;
            }

            JSONObject one = new JSONObject();
            one.put("old", oldVal);
            one.put("new", newVal);
            diff.put(field, one);
        });

        // 没有任何变化，不写日志
        if (diff.isEmpty()) return;

        // 插入一条审计日志
        try {
            // 使用异步方式插入审计日志，避免阻塞主事务
            CompletableFuture.runAsync(() -> {
                try {
                    jdbcTemplate.update(
                        "INSERT INTO sys_data_audit_log(table_name,row_id,operator,operate_time,ip,diff_json)" +
                        " VALUES(?,?,?,?,?,?)",
                        tableName,
                        oldRow.get(primaryKey), // 使用传入的主键字段名
                        LoginHelper.getUsername(),
                        new Date(),
                        ServletUtils.getClientIP(),
                        diff.toString()
                    );
                } catch (Exception e) {
                    log.error("异步保存审计日志失败，表：{}，错误：{}", tableName, e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("提交审计日志异步任务失败，表：{}，错误：{}", tableName, e.getMessage());
        }
    }
}
