-- =============================================
-- 单据关系图 - 完整查询方案（含实际单据编号）
-- 版本: V2.0
-- 日期: 2026-04-14
-- 说明: 使用存储过程动态查询每个节点的实际单据编号
-- =============================================

DROP PROCEDURE IF EXISTS `sp_get_bill_relationship_graph`;

DELIMITER $$

CREATE PROCEDURE `sp_get_bill_relationship_graph`(
    IN p_table_code VARCHAR(100),
    IN p_bill_no VARCHAR(100)
)
BEGIN
    -- 声明变量（必须在最前面）
    DECLARE v_done INT DEFAULT FALSE;
    DECLARE v_table_code VARCHAR(100);
    DECLARE v_bill_name VARCHAR(200);
    DECLARE v_bill_no_field VARCHAR(50);
    DECLARE v_pk_field VARCHAR(50);
    DECLARE v_node_type VARCHAR(50);
    DECLARE v_actual_bill_no VARCHAR(100);
    DECLARE v_sql_text TEXT;
    DECLARE v_counter INT DEFAULT 0;
    DECLARE v_total_count INT DEFAULT 0;
    
    -- 声明handler
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = TRUE;
    
    -- 临时表：存储关系链节点
    DROP TEMPORARY TABLE IF EXISTS tmp_bill_chain;
    CREATE TEMPORARY TABLE tmp_bill_chain (
        table_code VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
        bill_name VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
        sort_num INT,
        depth INT,
        node_type VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
        bill_no_field VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
        pk_field VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci
    ) ENGINE=MEMORY CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
    
    -- 临时表：存储最终结果（含实际单据编号）
    DROP TEMPORARY TABLE IF EXISTS tmp_bill_result;
    CREATE TEMPORARY TABLE tmp_bill_result (
        id VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
        name VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
        category INT,
        symbolSize INT,
        moduleCode VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
        billName VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
        nodeType VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
        sortNum INT,
        depth INT,
        color VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
        billNoField VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
        billNo VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci
    ) ENGINE=MEMORY CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
    
    -- ===================== 步骤1: 递归查询关系链 =====================
    INSERT IGNORE INTO tmp_bill_chain (table_code, bill_name, sort_num, depth, node_type, bill_no_field, pk_field)
    WITH RECURSIVE bill_chain AS (
        -- 初始节点
        SELECT
            d.source AS table_code,
            c.bill_name,
            c.sort_num,
            0 AS depth,
            CAST('current' AS CHAR(20)) AS node_type,
            c.bill_no_field,
            c.pk_field
        FROM t_identifier_dictionary d
        INNER JOIN erp_bill_relation_config c 
            ON d.source COLLATE utf8mb4_0900_ai_ci = c.table_code COLLATE utf8mb4_0900_ai_ci 
            AND c.enable=1
        WHERE d.source = p_table_code

        UNION ALL

        -- 上游
        SELECT
            p.source,
            c2.bill_name,
            c2.sort_num,
            bc.depth+1,
            CAST('predecessor' AS CHAR(20)) AS node_type,
            c2.bill_no_field,
            c2.pk_field
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
            CAST('successor' AS CHAR(20)) AS node_type,
            c3.bill_no_field,
            c3.pk_field
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
        node_type,
        bill_no_field,
        pk_field
    FROM bill_chain
    WHERE table_code IS NOT NULL AND table_code != '';
    
    -- ===================== 步骤2: 动态查询每个节点的实际单据编号 =====================
    -- 获取节点总数
    SELECT COUNT(*) INTO v_total_count FROM tmp_bill_chain;
    
    -- 使用WHILE循环代替游标
    WHILE v_counter < v_total_count DO
        -- 获取下一个节点信息
        SELECT table_code, bill_name, bill_no_field, pk_field, node_type
        INTO v_table_code, v_bill_name, v_bill_no_field, v_pk_field, v_node_type
        FROM tmp_bill_chain
        ORDER BY sort_num
        LIMIT v_counter, 1;
        
        SET v_counter = v_counter + 1;
        
        -- 初始化实际单据编号
        SET v_actual_bill_no = NULL;
        
        -- 当前节点：直接使用传入的单据编号
        IF v_node_type = 'current' THEN
            SET v_actual_bill_no = p_bill_no;
        ELSE
            -- 其他节点：需要通过关联字段查询
            -- 构建动态SQL：根据node_type决定查询方向
            IF v_node_type = 'predecessor' THEN
                -- 上游：查找 goal = 当前节点的记录，获取其 source 表的单据
                SET v_sql_text = CONCAT(
                    'SELECT t.', v_bill_no_field, 
                    ' INTO @v_bill_no ',
                    'FROM ', v_table_code, ' t ',
                    'INNER JOIN t_identifier_dictionary dict ',
                    'ON t.', v_bill_no_field, ' COLLATE utf8mb4_0900_ai_ci = dict.source COLLATE utf8mb4_0900_ai_ci ',
                    'WHERE dict.goal COLLATE utf8mb4_0900_ai_ci = ''', p_table_code, ''' ',
                    'AND dict.source COLLATE utf8mb4_0900_ai_ci = ''', v_table_code, ''' ',
                    'LIMIT 1'
                );
            ELSEIF v_node_type = 'successor' THEN
                -- 下游：查找 source = 当前节点的记录，获取其 goal 表的单据
                SET v_sql_text = CONCAT(
                    'SELECT t.', v_bill_no_field,
                    ' INTO @v_bill_no ',
                    'FROM ', v_table_code, ' t ',
                    'INNER JOIN t_identifier_dictionary dict ',
                    'ON t.', v_bill_no_field, ' COLLATE utf8mb4_0900_ai_ci = dict.goal COLLATE utf8mb4_0900_ai_ci ',
                    'WHERE dict.source COLLATE utf8mb4_0900_ai_ci = ''', p_table_code, ''' ',
                    'AND dict.goal COLLATE utf8mb4_0900_ai_ci = ''', v_table_code, ''' ',
                    'LIMIT 1'
                );
            END IF;
            
            -- 执行动态SQL
            IF v_sql_text IS NOT NULL THEN
                BEGIN
                    DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        SET v_actual_bill_no = NULL;
                    END;
                    
                    SET @v_bill_no = NULL;
                    SET @sql_exec = v_sql_text;
                    PREPARE stmt FROM @sql_exec;
                    EXECUTE stmt;
                    DEALLOCATE PREPARE stmt;
                    
                    SET v_actual_bill_no = @v_bill_no;
                END;
            END IF;
        END IF;
        
        -- 插入结果表
        INSERT INTO tmp_bill_result (
            id, name, category, symbolSize, moduleCode, billName, 
            nodeType, sortNum, depth, color, billNoField, billNo
        )
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
            tc.bill_no_field AS billNoField,
            v_actual_bill_no AS billNo
        FROM tmp_bill_chain tc
        WHERE tc.table_code = v_table_code;
        
    END WHILE;
    
    -- ===================== 输出 Nodes =====================
    SELECT 
        id,
        name,
        category,
        symbolSize,
        moduleCode,
        billName,
        nodeType,
        sortNum,
        depth,
        color,
        billNoField,
        billNo
    FROM tmp_bill_result
    ORDER BY sortNum;
    
    -- ===================== 输出 Links =====================
    SELECT 
        d.source,
        d.goal AS target,
        CONCAT(cfg_s.bill_name, '→', cfg_g.bill_name) AS label_formatter,
        'source' AS line_color,
        0.2 AS curveness,
        2 AS line_width
    FROM t_identifier_dictionary d
    INNER JOIN tmp_bill_chain tc_s ON d.source = tc_s.table_code
    INNER JOIN tmp_bill_chain tc_g ON d.goal = tc_g.table_code
    LEFT JOIN erp_bill_relation_config cfg_s ON d.source = cfg_s.table_code
    LEFT JOIN erp_bill_relation_config cfg_g ON d.goal = cfg_g.table_code
    WHERE d.goal IS NOT NULL AND d.goal != '';
    
    -- 清理临时表
    DROP TEMPORARY TABLE IF EXISTS tmp_bill_chain;
    DROP TEMPORARY TABLE IF EXISTS tmp_bill_result;
    
END$$

DELIMITER ;

-- =============================================
-- 调用示例
-- =============================================
-- CALL sp_get_bill_relationship_graph('t_sale_xsddxx', 'VI20211111');
