package com.ruoyi.erp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

/**
 * ERP 字典 Mapper - 无 UNION 纯净版
 * 彻底解决 Illegal mix of collations 报错
 */
@Mapper
public interface ErpDictionaryMapper {

    // ==================== 系统字典查询 ====================
    
    /**
     * 查询系统字典数据（带条件）
     */
    @Select("<script>" +
            "SELECT dict_label AS label, dict_value AS value, dict_type AS type " +
            "FROM sys_dict_data " +
            "<if test='dictType != null and dictType != \"\"'>WHERE dict_type = #{dictType}</if> " +
            "ORDER BY dict_sort" +
            "</script>")
    List<Map<String, Object>> selectSysDictData(@Param("dictType") String dictType);

    /**
     * 查询系统字典类型（带条件）
     */
    @Select("<script>" +
            "SELECT dict_name AS label, dict_type AS value, dict_type AS type " +
            "FROM sys_dict_type " +
            "<if test='dictType != null and dictType != \"\"'>WHERE dict_type = #{dictType}</if> " +
            "ORDER BY dict_id" +
            "</script>")
    List<Map<String, Object>> selectDictTypes(@Param("dictType") String dictType);

    // ==================== 业务字典查询 ====================
    
    /**
     * 查询业务字典数据（带条件）
     */
    @Select("<script>" +
            "SELECT name AS label, kingdee AS value, category AS type " +
            "FROM bymaterial_dictionary " +
            "<if test='category != null and category != \"\"'>WHERE category = #{category}</if> " +
            "ORDER BY id" +
            "</script>")
    List<Map<String, Object>> selectBizDictData(@Param("category") String category);

    /**
     * 根据分类查询业务字典
     */
    @Select("<script>" +
            "SELECT id, name AS label, kingdee AS value, category AS type, code, parent_code AS parentCode " +
            "FROM bymaterial_dictionary WHERE category = #{category} ORDER BY id ASC" +
            "</script>")
    List<Map<String, Object>> selectBizDictByCategory(@Param("category") String category);

    /**
     * 自定义值字段的业务字典查询
     */
    @Select("<script>" +
            "SELECT id, name AS label, ${valueField} AS value, category AS type, code " +
            "FROM bymaterial_dictionary WHERE category = #{category} ORDER BY id ASC" +
            "</script>")
    List<Map<String, Object>> selectBizDictCustom(
        @Param("category") String category, 
        @Param("valueField") String valueField);

    // ==================== 国家字典查询 ====================
    
    /**
     * 根据国家 ID 查询
     */
    @Select("<script>" +
            "SELECT id, name_en AS labelEn, name_zh AS labelZh, status " +
            "FROM country WHERE id = #{id} AND status = 1" +
            "</script>")
    Map<String, Object> selectCountryById(@Param("id") Long id);

    /**
     * 查询所有可用国家
     */
    @Select("SELECT id, name_en AS labelEn, name_zh AS labelZh, status " +
            "FROM country WHERE status = 1 ORDER BY name_zh ASC")
    List<Map<String, Object>> selectAllCountries();

    /**
     * 搜索国家（支持关键字和分页）
     */
    @Select("<script>" +
            "SELECT id, name_en AS labelEn, name_zh AS labelZh, status " +
            "FROM country WHERE status = 1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (name_zh LIKE CONCAT('%', #{keyword}, '%') OR name_en LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY name_zh ASC " +
            "<if test='limit != null'>LIMIT #{limit}</if>" +
            "</script>")
    List<Map<String, Object>> searchCountries(
        @Param("keyword") String keyword, 
        @Param("limit") Integer limit);

    // ==================== 销售人员字典查询 ====================
    
    /**
     * 查询销售人员字典数据
     * 包含部门、角色、工号等信息
     * 
     * ⚠️ **重要说明**:
     * - value: user_id (雪花算法 ID, BIGINT)
     * - FSalerId: salesman_id (销售员编码，VARCHAR) ✅ 使用数据库字段名
     */
    @Select("SELECT " +
            "  CONCAT(u.nick_name, IFNULL(CONCAT('(', d.dept_name, ')'), ''), IFNULL(CONCAT(' - ', e.salesman_id), '')) AS label, " +
            "  COALESCE(e.salesman_id, CAST(u.user_id AS CHAR)) AS value, " +  // 优先使用 salesman_id，没有则使用 user_id
            "  'salespersons' AS type, " +
            "  u.nick_name AS nickName, " +
            "  d.dept_name AS departmentName, " +
            "  e.salesman_id AS FSalerId, " +  // 销售员编码
            "  GROUP_CONCAT(DISTINCT sr.role_name ORDER BY sr.role_id) AS roleNames " +
            "FROM sys_user u " +
            "LEFT JOIN sys_dept d ON u.dept_id = d.dept_id " +
            "LEFT JOIN sys_user_role ur ON u.user_id = ur.user_id " +
            "LEFT JOIN sys_role sr ON ur.role_id = sr.role_id " +
            "LEFT JOIN sys_employee e ON u.staff_id = e.fid " +
            "WHERE ( " +
            "  u.dept_id IN ('1995775271620800513', '1995776039019048962', '1995776549579091969', '1995776618810273794') " +
            "  OR ur.role_id IN (1, 2016378335548186625, 2021458108021686273) " +
            ") " +
            "AND u.status = '1' " +
            "AND u.del_flag = '0' " +
            "GROUP BY u.user_id, u.nick_name, u.staff_id, d.dept_name, e.salesman_id " +
            "ORDER BY u.nick_name")
    List<Map<String, Object>> selectSalespersonsDict();
}
