SET @in_table_code = 't_sale_xsddxx';
SET @in_bill_no = 'VI20211111';

DROP TEMPORARY TABLE IF EXISTS tmp_bill_chain;
CREATE TEMPORARY TABLE tmp_bill_chain (
    table_code VARCHAR(100),
    bill_name VARCHAR(200),
    sort_num INT,
    depth INT,
    node_type VARCHAR(50)  -- 足够长，绝对不截断
) ENGINE=MEMORY;

-- 核心：用 INSERT IGNORE + 强制干净数据，杜绝截断/超长
INSERT IGNORE INTO tmp_bill_chain (table_code, bill_name, sort_num, depth, node_type)
WITH RECURSIVE bill_chain AS (
    -- 初始节点
    SELECT
        d.source AS table_code,
        c.bill_name,
        c.sort_num,
        0 AS depth,
        CAST('current' AS CHAR(20)) AS node_type
    FROM t_identifier_dictionary d
    INNER JOIN erp_bill_relation_config c 
        ON d.source COLLATE utf8mb4_0900_ai_ci = c.table_code COLLATE utf8mb4_0900_ai_ci 
        AND c.enable=1
    WHERE d.source = @in_table_code

    UNION ALL

    -- 上游
    SELECT
        p.source,
        c2.bill_name,
        c2.sort_num,
        bc.depth+1,
        CAST('predecessor' AS CHAR(20)) AS node_type
    FROM t_identifier_dictionary p
    INNER JOIN erp_bill_relation_config c2 
        ON p.source COLLATE utf8mb4_0900_ai_ci = c2.table_code COLLATE utf8mb4_0900_ai_ci 
        AND c2.enable=1
    INNER JOIN bill_chain bc 
        ON p.goal COLLATE utf8mb4_0900_ai_ci = bc.table_code COLLATE utf8mb4_0900_ai_ci
    WHERE bc.node_type != 'successor' AND bc.depth < 20

    UNION ALL

    -- 下游
    SELECT
        n.goal,
        c3.bill_name,
        c3.sort_num,
        bc.depth+1,
        CAST('successor' AS CHAR(20)) AS node_type
    FROM t_identifier_dictionary n
    INNER JOIN erp_bill_relation_config c3 
        ON n.goal COLLATE utf8mb4_0900_ai_ci = c3.table_code COLLATE utf8mb4_0900_ai_ci 
        AND c3.enable=1
    INNER JOIN bill_chain bc 
        ON n.source COLLATE utf8mb4_0900_ai_ci = bc.table_code COLLATE utf8mb4_0900_ai_ci
    WHERE n.goal IS NOT NULL AND n.goal != '' AND bc.node_type != 'predecessor' AND bc.depth < 20
)
SELECT DISTINCT
    table_code,
    bill_name,
    sort_num,
    depth,
    node_type
FROM bill_chain
WHERE table_code IS NOT NULL AND table_code!='';

-- ===================== 输出 Nodes =====================
SELECT 
    tc.table_code AS id,
    tc.bill_name AS name,
    CASE tc.node_type
        WHEN 'current' THEN 0
        WHEN 'predecessor' THEN 1
        WHEN 'successor' THEN 2
        ELSE 3
    END AS category,
    CASE tc.node_type WHEN 'current' THEN 80 ELSE 60 END AS symbolSize,
    tc.table_code AS moduleCode,
    tc.bill_name AS billName,
    tc.node_type AS nodeType,
    tc.sort_num AS sortNum,
    tc.depth,
    CASE tc.node_type
        WHEN 'current' THEN '#409EFF'
        WHEN 'predecessor' THEN '#67C23A'
        WHEN 'successor' THEN '#E6A23C'
        ELSE '#909399'
    END AS color,
    cfg.bill_no_field AS billNoField,
    -- 注意：这里返回字段名，实际值需要前端根据moduleCode和billNoField动态查询
    NULL AS billNo
FROM tmp_bill_chain tc
LEFT JOIN erp_bill_relation_config cfg 
    ON tc.table_code COLLATE utf8mb4_0900_ai_ci = cfg.table_code COLLATE utf8mb4_0900_ai_ci
ORDER BY tc.sort_num;

-- ===================== 输出 Links =====================
SELECT 
    d.source,
    d.goal AS target,
    CONCAT(cfg_s.bill_name, '→', cfg_g.bill_name) AS label_formatter,
    'source' line_color, 0.2 curveness, 2 line_width
FROM t_identifier_dictionary d
INNER JOIN tmp_bill_chain tc_s 
    ON d.source COLLATE utf8mb4_0900_ai_ci = tc_s.table_code COLLATE utf8mb4_0900_ai_ci
INNER JOIN tmp_bill_chain tc_g 
    ON d.goal COLLATE utf8mb4_0900_ai_ci = tc_g.table_code COLLATE utf8mb4_0900_ai_ci
LEFT JOIN erp_bill_relation_config cfg_s 
    ON d.source COLLATE utf8mb4_0900_ai_ci = cfg_s.table_code COLLATE utf8mb4_0900_ai_ci
LEFT JOIN erp_bill_relation_config cfg_g 
    ON d.goal COLLATE utf8mb4_0900_ai_ci = cfg_g.table_code COLLATE utf8mb4_0900_ai_ci
WHERE d.goal IS NOT NULL AND d.goal != '';

DROP TEMPORARY TABLE IF EXISTS tmp_bill_chain;