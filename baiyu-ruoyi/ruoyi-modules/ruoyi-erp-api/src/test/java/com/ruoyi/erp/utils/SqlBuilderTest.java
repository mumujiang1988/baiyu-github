package com.ruoyi.erp.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SqlBuilder 单元测试类
 * 
 * 测试覆盖场景：
 * 1. 基本操作符测试（eq/ne/gt/ge/lt/le）
 * 2. 模糊查询测试（like/left_like/right_like）
 * 3. 范围查询测试（between/in）
 * 4. 空值测试（isNull/isNotNull）
 * 5. 排序测试（orderBy）
 * 6. SQL 注入防护测试
 * 7. 边界条件测试
 * 
 * @author JMH
 * @date 2026-03-30
 */
@SpringBootTest
@DisplayName("SqlBuilder 单元测试")
class SqlBuilderTest {

    @Autowired
    private SqlBuilder sqlBuilder;

    @BeforeEach
    void setUp() {
        assertNotNull(sqlBuilder, "SqlBuilder 注入失败");
    }

    // ==================== 基本操作符测试 ====================

    @Test
    @DisplayName("测试等于操作符 (eq)")
    void testBuildWhere_Eq() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "status");
        condition.put("operator", "eq");
        condition.put("value", "1");
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertTrue(result.getSql().contains("status = ?"));
        assertEquals(1, result.getParamCount());
        assertEquals("1", result.getParams().get(0));
    }

    @Test
    @DisplayName("测试不等于操作符 (ne)")
    void testBuildWhere_Ne() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "status");
        condition.put("operator", "ne");
        condition.put("value", "0");
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertTrue(result.getSql().contains("status <> ?"));
        assertEquals(1, result.getParamCount());
    }

    @Test
    @DisplayName("测试大于操作符 (gt)")
    void testBuildWhere_Gt() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "price");
        condition.put("operator", "gt");
        condition.put("value", 100);
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertTrue(result.getSql().contains("price > ?"));
        assertEquals(1, result.getParamCount());
        assertEquals(100, result.getParams().get(0));
    }

    @Test
    @DisplayName("测试大于等于操作符 (ge)")
    void testBuildWhere_Ge() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "quantity");
        condition.put("operator", "ge");
        condition.put("value", 10);
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertTrue(result.getSql().contains("quantity >= ?"));
        assertEquals(1, result.getParamCount());
    }

    @Test
    @DisplayName("测试小于操作符 (lt)")
    void testBuildWhere_Lt() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "age");
        condition.put("operator", "lt");
        condition.put("value", 18);
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertTrue(result.getSql().contains("age < ?"));
        assertEquals(1, result.getParamCount());
    }

    @Test
    @DisplayName("测试小于等于操作符 (le)")
    void testBuildWhere_Le() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "discount");
        condition.put("operator", "le");
        condition.put("value", 0.8);
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertTrue(result.getSql().contains("discount <= ?"));
        assertEquals(1, result.getParamCount());
    }

    // ==================== 模糊查询测试 ====================

    @Test
    @DisplayName("测试模糊匹配 (like)")
    void testBuildWhere_Like() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "customer_name");
        condition.put("operator", "like");
        condition.put("value", "张三");
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertTrue(result.getSql().contains("customer_name LIKE ?"));
        assertEquals(1, result.getParamCount());
        assertEquals("%张三%", result.getParams().get(0));
    }

    @Test
    @DisplayName("测试左模糊匹配 (left_like)")
    void testBuildWhere_LeftLike() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "address");
        condition.put("operator", "left_like");
        condition.put("value", "北京");
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertTrue(result.getSql().contains("address LIKE ?"));
        assertEquals("%北京", result.getParams().get(0));
    }

    @Test
    @DisplayName("测试右模糊匹配 (right_like)")
    void testBuildWhere_RightLike() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "phone");
        condition.put("operator", "right_like");
        condition.put("value", "1234");
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertTrue(result.getSql().contains("phone LIKE ?"));
        assertEquals("1234%", result.getParams().get(0));
    }

    @Test
    @DisplayName("测试 LIKE 特殊字符转义")
    void testBuildWhere_LikeEscape() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "description");
        condition.put("operator", "like");
        condition.put("value", "100%");
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertEquals("100\\%", result.getParams().get(0));
    }

    // ==================== 范围查询测试 ====================

    @Test
    @DisplayName("测试 BETWEEN 操作符")
    void testBuildWhere_Between() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "create_time");
        condition.put("operator", "between");
        condition.put("value", Arrays.asList("2026-03-01", "2026-03-30"));
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertTrue(result.getSql().contains("create_time BETWEEN ? AND ?"));
        assertEquals(2, result.getParamCount());
        assertEquals("2026-03-01", result.getParams().get(0));
        assertEquals("2026-03-30", result.getParams().get(1));
    }

    @Test
    @DisplayName("测试 IN 操作符")
    void testBuildWhere_In() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "status");
        condition.put("operator", "in");
        condition.put("value", Arrays.asList("1", "2", "3"));
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertTrue(result.getSql().contains("status IN ("));
        assertEquals(3, result.getParamCount());
    }

    @Test
    @DisplayName("测试 IN 操作符 - 空列表")
    void testBuildWhere_In_EmptyList() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "status");
        condition.put("operator", "in");
        condition.put("value", new ArrayList<>());
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertEquals(0, result.getParamCount());
        assertFalse(result.getSql().contains("IN"));
    }

    // ==================== 空值测试 ====================

    @Test
    @DisplayName("测试 IS NULL 操作符")
    void testBuildWhere_IsNull() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "remark");
        condition.put("operator", "isNull");
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertTrue(result.getSql().contains("remark IS NULL"));
        assertEquals(0, result.getParamCount());
    }

    @Test
    @DisplayName("测试 IS NOT NULL 操作符")
    void testBuildWhere_IsNotNull() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "email");
        condition.put("operator", "isNotNull");
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertTrue(result.getSql().contains("email IS NOT NULL"));
        assertEquals(0, result.getParamCount());
    }

    // ==================== 组合条件测试 ====================

    @Test
    @DisplayName("测试多个条件组合")
    void testBuildWhere_MultipleConditions() {
        List<Map<String, Object>> conditions = new ArrayList<>();

        // 条件 1: status = '1'
        Map<String, Object> condition1 = new HashMap<>();
        condition1.put("field", "status");
        condition1.put("operator", "eq");
        condition1.put("value", "1");
        conditions.add(condition1);

        // 条件 2: price > 100
        Map<String, Object> condition2 = new HashMap<>();
        condition2.put("field", "price");
        condition2.put("operator", "gt");
        condition2.put("value", 100);
        conditions.add(condition2);

        // 条件 3: customer_name LIKE '%张%'
        Map<String, Object> condition3 = new HashMap<>();
        condition3.put("field", "customer_name");
        condition3.put("operator", "like");
        condition3.put("value", "张");
        conditions.add(condition3);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertEquals(3, result.getParamCount());
        assertTrue(result.getSql().contains("status = ?"));
        assertTrue(result.getSql().contains("price > ?"));
        assertTrue(result.getSql().contains("customer_name LIKE ?"));
    }

    @Test
    @DisplayName("测试空条件列表")
    void testBuildWhere_EmptyConditions() {
        List<Map<String, Object>> conditions = new ArrayList<>();

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertEquals(0, result.getParamCount());
        assertEquals("", result.getSql());
    }

    @Test
    @DisplayName("测试 null 条件列表")
    void testBuildWhere_NullConditions() {
        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(null);

        assertNotNull(result);
        assertEquals(0, result.getParamCount());
        assertEquals("", result.getSql());
    }

    // ==================== 排序测试 ====================

    @Test
    @DisplayName("测试 ORDER BY ASC")
    void testBuildOrderBy_Asc() {
        List<Map<String, Object>> orderBy = new ArrayList<>();
        Map<String, Object> order = new HashMap<>();
        order.put("field", "create_time");
        order.put("direction", "asc");
        orderBy.add(order);

        SqlBuilder.SqlResult result = sqlBuilder.buildOrderBy(orderBy);

        assertNotNull(result);
        assertTrue(result.getSql().contains("ORDER BY create_time ASC"));
        assertEquals(0, result.getParamCount());
    }

    @Test
    @DisplayName("测试 ORDER BY DESC")
    void testBuildOrderBy_Desc() {
        List<Map<String, Object>> orderBy = new ArrayList<>();
        Map<String, Object> order = new HashMap<>();
        order.put("field", "update_time");
        order.put("direction", "desc");
        orderBy.add(order);

        SqlBuilder.SqlResult result = sqlBuilder.buildOrderBy(orderBy);

        assertNotNull(result);
        assertTrue(result.getSql().contains("ORDER BY update_time DESC"));
    }

    @Test
    @DisplayName("测试多个 ORDER BY")
    void testBuildOrderBy_Multiple() {
        List<Map<String, Object>> orderBy = new ArrayList<>();

        Map<String, Object> order1 = new HashMap<>();
        order1.put("field", "status");
        order1.put("direction", "asc");
        orderBy.add(order1);

        Map<String, Object> order2 = new HashMap<>();
        order2.put("field", "create_time");
        order2.put("direction", "desc");
        orderBy.add(order2);

        SqlBuilder.SqlResult result = sqlBuilder.buildOrderBy(orderBy);

        assertNotNull(result);
        assertTrue(result.getSql().contains("ORDER BY status ASC, create_time DESC"));
    }

    @Test
    @DisplayName("测试空排序列表")
    void testBuildOrderBy_Empty() {
        List<Map<String, Object>> orderBy = new ArrayList<>();

        SqlBuilder.SqlResult result = sqlBuilder.buildOrderBy(orderBy);

        assertNotNull(result);
        assertEquals("", result.getSql());
    }

    // ==================== SQL 注入防护测试 ====================

    @Test
    @DisplayName("测试 SQL 注入防护 - DROP TABLE")
    void testValidateFieldName_DropTable() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "status; DROP TABLE users");
        condition.put("operator", "eq");
        condition.put("value", "1");
        conditions.add(condition);

        assertThrows(IllegalArgumentException.class, () -> {
            sqlBuilder.buildWhere(conditions);
        });
    }

    @Test
    @DisplayName("测试 SQL 注入防护 - 注释符")
    void testValidateFieldName_Comment() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "status--");
        condition.put("operator", "eq");
        condition.put("value", "1");
        conditions.add(condition);

        assertThrows(IllegalArgumentException.class, () -> {
            sqlBuilder.buildWhere(conditions);
        });
    }

    @Test
    @DisplayName("测试 SQL 注入防护 - 特殊字符")
    void testValidateFieldName_SpecialChars() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "status/*test*/");
        condition.put("operator", "eq");
        condition.put("value", "1");
        conditions.add(condition);

        assertThrows(IllegalArgumentException.class, () -> {
            sqlBuilder.buildWhere(conditions);
        });
    }

    @Test
    @DisplayName("测试合法字段名 - 下划线")
    void testValidateFieldName_Underscore() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "user_name");
        condition.put("operator", "eq");
        condition.put("value", "test");
        conditions.add(condition);

        assertDoesNotThrow(() -> {
            sqlBuilder.buildWhere(conditions);
        });
    }

    @Test
    @DisplayName("测试合法字段名 - 驼峰命名")
    void testValidateFieldName_CamelCase() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "userName");
        condition.put("operator", "eq");
        condition.put("value", "test");
        conditions.add(condition);

        assertDoesNotThrow(() -> {
            sqlBuilder.buildWhere(conditions);
        });
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("测试空字段名")
    void testBuildWhere_EmptyField() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "");
        condition.put("operator", "eq");
        condition.put("value", "1");
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertEquals(0, result.getParamCount());
    }

    @Test
    @DisplayName("测试 null 值")
    void testBuildWhere_NullValue() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "status");
        condition.put("operator", "eq");
        condition.put("value", null);
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertEquals(0, result.getParamCount());
    }

    @Test
    @DisplayName("测试未知操作符")
    void testBuildWhere_UnknownOperator() {
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("field", "status");
        condition.put("operator", "unknown");
        condition.put("value", "1");
        conditions.add(condition);

        SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);

        assertNotNull(result);
        assertEquals(0, result.getParamCount());
    }
}
