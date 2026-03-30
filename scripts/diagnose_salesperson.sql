-- =====================================================
-- 销售人员编码查询诊断 SQL
-- 生成时间：2026-03-30
-- 目的：确认 100949 和 100055 的对应关系
-- =====================================================

-- 1. 查询所有销售员及其对应关系
SELECT 
  u.user_id,
  u.nick_name,
  e.salesman_id,
  d.dept_name,
  u.staff_id,
  u.status,
  u.del_flag
FROM sys_user u
LEFT JOIN sys_employee e ON u.staff_id = e.fid
LEFT JOIN sys_dept d ON u.dept_id = d.dept_id
WHERE e.salesman_id IN ('100949', '100055')
   OR u.user_id IN (100949, 100055)
ORDER BY e.salesman_id;

-- 2. 查询完整的销售人员字典数据（模拟后端接口）
SELECT 
  CONCAT(u.nick_name, IFNULL(CONCAT('(', d.dept_name, ')'), ''), 
         IFNULL(CONCAT(' - ', e.salesman_id), '')) AS label,
  u.user_id AS value,
  e.salesman_id AS fseller,
  u.nick_name AS nickName,
  d.dept_name AS departmentName,
  u.staff_id
FROM sys_user u
LEFT JOIN sys_dept d ON u.dept_id = d.dept_id
LEFT JOIN sys_user_role ur ON u.user_id = ur.user_id
LEFT JOIN sys_role sr ON ur.role_id = sr.role_id
LEFT JOIN sys_employee e ON u.staff_id = e.fid
WHERE (
  u.dept_id IN ('1995775271620800513', '1995776039019048962', 
                '1995776549579091969', '1995776618810273794')
  OR ur.role_id IN (1, 2016378335548186625, 2021458108021686273)
)
AND u.status = '1'
AND u.del_flag = '0'
GROUP BY u.user_id, u.nick_name, u.staff_id, d.dept_name, e.salesman_id
ORDER BY u.nick_name;

-- 3. 验证特定销售员是否存在
SELECT 
  '通过 salesman_id 查询' AS query_type,
  e.salesman_id,
  u.user_id,
  u.nick_name,
  d.dept_name
FROM sys_employee e
LEFT JOIN sys_user u ON e.fid = u.staff_id
LEFT JOIN sys_dept d ON u.dept_id = d.dept_id
WHERE e.salesman_id = '100949'

UNION ALL

SELECT 
  '通过 user_id 查询' AS query_type,
  e.salesman_id,
  u.user_id,
  u.nick_name,
  d.dept_name
FROM sys_user u
LEFT JOIN sys_employee e ON u.staff_id = e.fid
LEFT JOIN sys_dept d ON u.dept_id = d.dept_id
WHERE u.user_id = 100949

UNION ALL

SELECT 
  '通过 salesman_id 查询' AS query_type,
  e.salesman_id,
  u.user_id,
  u.nick_name,
  d.dept_name
FROM sys_employee e
LEFT JOIN sys_user u ON e.fid = u.staff_id
LEFT JOIN sys_dept d ON u.dept_id = d.dept_id
WHERE e.salesman_id = '100055'

UNION ALL

SELECT 
  '通过 user_id 查询' AS query_type,
  e.salesman_id,
  u.user_id,
  u.nick_name,
  d.dept_name
FROM sys_user u
LEFT JOIN sys_employee e ON u.staff_id = e.fid
LEFT JOIN sys_dept d ON u.dept_id = d.dept_id
WHERE u.user_id = 100055;

-- 4. 检查是否有重复的销售员编码
SELECT 
  e.salesman_id,
  COUNT(*) as cnt,
  GROUP_CONCAT(DISTINCT u.nick_name) as names,
  GROUP_CONCAT(DISTINCT u.user_id) as user_ids
FROM sys_employee e
LEFT JOIN sys_user u ON e.fid = u.staff_id
WHERE e.salesman_id IS NOT NULL AND e.salesman_id != ''
GROUP BY e.salesman_id
HAVING cnt > 1
ORDER BY cnt DESC;

-- 5. 查看所有可用的销售员编码范围
SELECT 
  LEFT(e.salesman_id, 3) AS prefix,
  COUNT(*) AS count,
  MIN(e.salesman_id) AS min_id,
  MAX(e.salesman_id) AS max_id
FROM sys_employee e
WHERE e.salesman_id IS NOT NULL AND e.salesman_id != ''
GROUP BY LEFT(e.salesman_id, 3)
ORDER BY prefix;

-- 6. 测试：根据值反查销售员信息（支持两种可能）
-- 方式 1: 假设传入的是 salesman_id
SELECT 
  '输入值作为 salesman_id' AS assumption,
  e.salesman_id AS input_value,
  u.user_id,
  u.nick_name,
  d.dept_name
FROM sys_employee e
LEFT JOIN sys_user u ON e.fid = u.staff_id
LEFT JOIN sys_dept d ON u.dept_id = d.dept_id
WHERE e.salesman_id = '100055'

UNION ALL

-- 方式 2: 假设传入的是 user_id
SELECT 
  '输入值作为 user_id' AS assumption,
  e.salesman_id AS actual_salesman_id,
  u.user_id AS input_value,
  u.nick_name,
  d.dept_name
FROM sys_user u
LEFT JOIN sys_employee e ON u.staff_id = e.fid
LEFT JOIN sys_dept d ON u.dept_id = d.dept_id
WHERE u.user_id = 100055;
