package com.ruoyi.erp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

/**
 * ERP 字典 Mapper - 无 UNION 纯净版 
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
     */
    @Select("SELECT " +
            "  u.nick_name AS label, " +  // 显示标签（人名）
            "  COALESCE(e.salesman_id, CAST(u.user_id AS CHAR)) AS value, " +  // 优先使用 salesman_id，没有则使用 user_id
            "  'salespersons' AS type, " +
            "  d.dept_name AS departmentName, " +  // 部门名称
            "  e.salesman_id AS FSalerId, " +  // 销售员编码
            "  GROUP_CONCAT(DISTINCT sr.role_name ORDER BY sr.role_id) AS roleNames " +  // 角色名称
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

    // ==================== 客户字典查询 ====================
    
    /**
     * 查询客户字典数据
     * 返回客户简称和客户编码
     */
    @Select("SELECT " +
            "  fname AS label, " +  // 客户全称
            "  CAST(fcustid AS CHAR) AS value, " +  // 客户 ID（转为字符串）
            "  'customers' AS type, " +
            "  fshort_name AS shortName, " +  // 客户简称
            "  fnumber AS customerCode " +  // 客户编码
            "FROM bd_customer " +
            "WHERE fdocumentStatus = 'C' " +  // 查询已创建/暂存的客户
            "ORDER BY fname ASC")
    List<Map<String, Object>> selectCustomersDict();

    // ==================== 物料字典查询 ====================
    
    /**
     * 查询物料字典数据
     * 返回物料名称、规格、编码等
     */
    @Select("SELECT " +
            "  name AS label, " +  // 物料名称
            "  number AS value, " +  // 物料编码
            "  'materials' AS type, " +
            "  specification AS specification, " +  // 规格型号
            "  product_category AS productCategory, " +  // 产品类别
            "  materialgroup AS materialGroup " +  // 物料分组
            "FROM by_material " +
            "WHERE f_state = '1' " +  // 只查询启用状态的物料
            "ORDER BY name ASC")
    List<Map<String, Object>> selectMaterialsDict();

    // ==================== 用户字典查询 ====================
    
    /**
     * 查询用户字典数据（所有正常用户）
     * 用于需要选择用户的场景
     */
    @Select("SELECT " +
            "  u.nick_name AS label, " +  // 显示标签（人名）
            "  u.k3_key AS value, " +  //  使用 k3_key（金蝶 K3 编码）作为 value
            "  'users' AS type, " +
            "  u.user_name AS userName, " +  // 登录账号
            "  d.dept_name AS departmentName, " +  // 部门名称
            "  u.email AS email, " +  // 邮箱
            "  u.phonenumber AS phonenumber " +  // 手机号
            "FROM sys_user u " +
            "LEFT JOIN sys_dept d ON u.dept_id = d.dept_id " + 
            "ORDER BY u.nick_name ASC")
    List<Map<String, Object>> selectUsersDict();

    // ==================== 供应商字典查询 ====================
    
    /**
     * 查询供应商字典数据
     * 返回供应商名称、简称、编码等
     */
    @Select("SELECT " +
            "  name AS label, " +  // 供应商名称
            "  CAST(supplierid AS CHAR) AS value, " +  // 供应商 ID（转为字符串）
            "  'suppliers' AS type, " +
            "  abbreviation AS shortName, " +  // 供应商简称
            "  number AS supplierCode " +  // 供应商编码
            "FROM supplier " +
            "WHERE supplierid IS NOT NULL AND supplierid != '' " +
            "ORDER BY name ASC")
    List<Map<String, Object>> selectSuppliersDict();

    // ==================== 部门字典查询 ====================
    
    /**
     * 查询部门字典数据
     * 返回部门名称和层级关系
     */
    @Select("SELECT " +
            "  dept_name AS label, " +  // 部门名称
            "  CAST(dept_id AS CHAR) AS value, " +  // 部门 ID（转为字符串）
            "  'departments' AS type, " +
            "  parent_id AS parentId, " +  // 父部门 ID
            "  ancestors AS ancestors, " +  // 祖列列表
            "  order_num AS orderNum " +  // 显示顺序
            "FROM sys_dept " + 
            "ORDER BY ancestors ASC, order_num ASC")
    List<Map<String, Object>> selectDepartmentsDict();

    // ==================== 税率字典查询 ====================
    
    /**
     * 查询税率字典数据
     * 返回税率名称、税率值、税制等信息
     */
    @Select("SELECT " +
            "  name AS label, " +  // 税率名称
            "  code AS value, " +  // 税率编码
            "  'tax_rates' AS type, " +
            "  tax_rate AS taxRate, " +  // 税率值（百分比）
            "  tax_system AS taxSystem, " +  // 税制
            "  tax_category AS taxCategory " +  // 税种分类
            "FROM tax_rate " +
            "ORDER BY tax_rate ASC, id ASC")
    List<Map<String, Object>> selectTaxRatesDict();
}
