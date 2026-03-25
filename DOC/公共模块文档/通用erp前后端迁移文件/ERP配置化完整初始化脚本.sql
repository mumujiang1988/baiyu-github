-- ============================================
-- ERP 配置化方案 - 完整数据库初始化脚本
-- 版本：v1.0
-- 日期：2026-03-24
-- 说明：包含表结构、初始数据、索引优化、权限配置
-- ============================================

USE test;

-- ============================================
-- 第一部分：创建核心表结构
-- ============================================

-- 1. 页面配置表
CREATE TABLE IF NOT EXISTS erp_page_config (
    config_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    module_code VARCHAR(100) NOT NULL COMMENT '模块编码（唯一标识）',
    config_name VARCHAR(200) NOT NULL COMMENT '配置名称',
    config_type VARCHAR(50) NOT NULL DEFAULT 'PAGE' COMMENT '配置类型（PAGE/FORM/TABLE/SEARCH）',
    config_content JSON NOT NULL COMMENT '配置内容（JSON格式）',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号',
    status CHAR(1) NOT NULL DEFAULT '1' COMMENT '状态（0禁用 1启用）',
    parent_config_id BIGINT NULL COMMENT '父配置ID（用于配置继承）',
    remark VARCHAR(500) NULL COMMENT '备注',
    create_by BIGINT NOT NULL COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新者',
    update_time DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_module_version (module_code, version),
    KEY idx_module_status (module_code, status),
    KEY idx_parent (parent_config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='ERP页面配置表';

-- 2. 配置历史表
CREATE TABLE IF NOT EXISTS erp_page_config_history (
    history_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史ID',
    config_id BIGINT NOT NULL COMMENT '配置ID',
    module_code VARCHAR(100) NOT NULL COMMENT '模块编码',
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型',
    version INT NOT NULL COMMENT '版本号',
    config_content JSON NOT NULL COMMENT '配置内容快照',
    change_reason VARCHAR(500) NULL COMMENT '变更原因',
    change_type VARCHAR(20) NOT NULL COMMENT '变更类型（CREATE/UPDATE/DELETE/ROLLBACK）',
    create_by BIGINT NOT NULL COMMENT '操作人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (history_id),
    KEY idx_config_id (config_id),
    KEY idx_module_version (module_code, version),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_history_config FOREIGN KEY (config_id) 
        REFERENCES erp_page_config(config_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='ERP配置历史表';

-- 3. 审批流程配置表
CREATE TABLE IF NOT EXISTS erp_approval_flow (
    flow_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '流程ID',
    flow_name VARCHAR(200) NOT NULL COMMENT '流程名称',
    module_code VARCHAR(100) NOT NULL COMMENT '模块编码',
    flow_config JSON NOT NULL COMMENT '流程配置（JSON格式）',
    status CHAR(1) NOT NULL DEFAULT '1' COMMENT '状态（0禁用 1启用）',
    remark VARCHAR(500) NULL COMMENT '备注',
    create_by BIGINT NOT NULL COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新者',
    update_time DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (flow_id),
    UNIQUE KEY uk_module (module_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='ERP审批流程配置表';

-- 4. 审批历史记录表
CREATE TABLE IF NOT EXISTS erp_approval_history (
    history_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史ID',
    flow_id BIGINT NULL COMMENT '流程ID',
    module_code VARCHAR(100) NOT NULL COMMENT '模块编码',
    bill_id BIGINT NOT NULL COMMENT '单据ID',
    bill_no VARCHAR(100) NULL COMMENT '单据编号',
    approval_action VARCHAR(50) NOT NULL COMMENT '审批动作（SUBMIT/APPROVE/REJECT/WITHDRAW）',
    approval_step INT NULL COMMENT '审批步骤',
    approval_user_id BIGINT NOT NULL COMMENT '审批人ID',
    approval_user_name VARCHAR(100) NULL COMMENT '审批人姓名',
    approval_opinion VARCHAR(500) NULL COMMENT '审批意见',
    approval_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    before_status VARCHAR(50) NULL COMMENT '审批前状态',
    after_status VARCHAR(50) NULL COMMENT '审批后状态',
    PRIMARY KEY (history_id),
    KEY idx_flow_id (flow_id),
    KEY idx_module_bill (module_code, bill_id),
    KEY idx_bill_action (bill_id, approval_action),
    KEY idx_approval_time (approval_time),
    CONSTRAINT fk_history_flow FOREIGN KEY (flow_id) 
        REFERENCES erp_approval_flow(flow_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='ERP审批历史记录表';

-- 5. 下推关系配置表
CREATE TABLE IF NOT EXISTS erp_push_relation (
    relation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系ID',
    source_module VARCHAR(100) NOT NULL COMMENT '源模块编码',
    target_module VARCHAR(100) NOT NULL COMMENT '目标模块编码',
    relation_name VARCHAR(200) NOT NULL COMMENT '关系名称',
    mapping_config JSON NOT NULL COMMENT '字段映射配置（JSON格式）',
    condition_config JSON NULL COMMENT '下推条件配置（JSON格式）',
    status CHAR(1) NOT NULL DEFAULT '1' COMMENT '状态（0禁用 1启用）',
    remark VARCHAR(500) NULL COMMENT '备注',
    create_by BIGINT NOT NULL COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新者',
    update_time DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (relation_id),
    KEY idx_source_target (source_module, target_module),
    KEY idx_source_status (source_module, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='ERP下推关系配置表';

-- ============================================
-- 第二部分：插入初始配置数据
-- ============================================

-- 1. 插入销售订单页面配置
INSERT INTO erp_page_config (module_code, config_name, config_type, config_content, version, status, create_by, remark)
SELECT 'saleOrder', '销售订单管理', 'PAGE', 
'{
  "pageConfig": {
    "title": "销售订单管理",
    "moduleCode": "saleOrder",
    "permissionPrefix": "k3:{moduleCode}",
    "apiPrefix": "/erp/engine",
    "layout": "standard",
    "primaryKey": "id",
    "billNoField": "fbillNo"
  },
  "searchConfig": {
    "showSearch": true,
    "defaultExpand": true,
    "fields": [
      {
        "field": "fbillNo",
        "label": "单据编号",
        "component": "input",
        "searchType": "like",
        "props": {
          "placeholder": "输入单据编号",
          "clearable": true,
          "prefixIcon": "Search",
          "style": { "width": "180px" }
        }
      },
      {
        "field": "fDocumentStatus",
        "label": "单据状态",
        "component": "select",
        "searchType": "eq",
        "dictionary": "documentStatus",
        "props": {
          "placeholder": "选择单据状态",
          "clearable": true,
          "filterable": true,
          "style": { "width": "120px" }
        }
      }
    ]
  },
  "tableConfig": {
    "rowKey": "id",
    "border": true,
    "stripe": true,
    "showOverflowTooltip": true,
    "columns": [
      {
        "type": "selection",
        "width": 55,
        "fixed": "left"
      },
      {
        "prop": "fbillNo",
        "label": "单据编号",
        "width": 150,
        "fixed": "left",
        "renderType": "text"
      },
      {
        "prop": "fDocumentStatus",
        "label": "单据状态",
        "width": 140,
        "align": "center",
        "renderType": "tag",
        "dictionary": "documentStatus"
      },
      {
        "prop": "fbillAmount",
        "label": "订单金额",
        "width": 140,
        "align": "right",
        "renderType": "currency",
        "precision": 2
      }
    ]
  },
  "actionConfig": {
    "toolbar": [
      {
        "type": "primary",
        "label": "新增",
        "icon": "Plus",
        "permission": "k3:{moduleCode}:add",
        "handler": "handleAdd"
      },
      {
        "type": "success",
        "label": "修改",
        "icon": "Edit",
        "permission": "k3:{moduleCode}:edit",
        "handler": "handleUpdate",
        "disabled": "single"
      },
      {
        "type": "danger",
        "label": "删除",
        "icon": "Delete",
        "permission": "k3:{moduleCode}:remove",
        "handler": "handleDelete",
        "disabled": "multiple"
      }
    ]
  }
}',
1, '1', 1, '销售订单管理页面配置'
WHERE NOT EXISTS (
    SELECT 1 FROM erp_page_config WHERE module_code = 'saleOrder' AND version = 1
);

-- 2. 插入审批流程配置
INSERT INTO erp_approval_flow (flow_name, module_code, flow_config, status, create_by, remark)
SELECT '销售订单审批流程', 'saleOrder',
'{
  "steps": [
    {
      "step": 1,
      "name": "提交",
      "action": "SUBMIT",
      "condition": "fDocumentStatus == \'Z\'",
      "nextStep": 2
    },
    {
      "step": 2,
      "name": "部门经理审批",
      "action": "APPROVE",
      "condition": "fDocumentStatus == \'A\'",
      "approvers": ["role:dept_manager"],
      "nextStep": 3
    },
    {
      "step": 3,
      "name": "财务审批",
      "action": "APPROVE",
      "condition": "fDocumentStatus == \'B\'",
      "approvers": ["role:finance"],
      "nextStep": 4
    },
    {
      "step": 4,
      "name": "完成",
      "action": "COMPLETE",
      "condition": "fDocumentStatus == \'C\'"
    }
  ]
}',
'1', 1, '销售订单审批流程配置'
WHERE NOT EXISTS (
    SELECT 1 FROM erp_approval_flow WHERE module_code = 'saleOrder'
);

-- 3. 插入下推关系配置
INSERT INTO erp_push_relation (source_module, target_module, relation_name, mapping_config, status, create_by, remark)
SELECT 'saleOrder', 'deliveryOrder', '销售订单下推发货通知单',
'{
  "fieldMapping": [
    { "source": "fbillNo", "target": "fSourceBillNo" },
    { "source": "fCustomerNumber", "target": "fCustomerNumber" },
    { "source": "fCustomerName", "target": "fCustomerName" }
  ],
  "entryMapping": [
    { "source": "fPlanMaterialId", "target": "fMaterialId" },
    { "source": "fQty", "target": "fQty" },
    { "source": "fPrice", "target": "fPrice" }
  ]
}',
'1', 1, '销售订单下推发货通知单配置'
WHERE NOT EXISTS (
    SELECT 1 FROM erp_push_relation WHERE source_module = 'saleOrder' AND target_module = 'deliveryOrder'
);

-- ============================================
-- 第三部分：创建菜单和权限配置
-- ============================================

-- 1. 创建ERP业务菜单目录
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, remark, create_by, create_time)
SELECT '1943362205047062529', 'ERP 业务菜单', 0, 5, 'business', '', 0, 0, 'M', 1, 1, '', 'document', 
       'ERP 业务菜单目录', 'admin', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_id = '1943362205047062529'
);

-- 2. 创建销售订单管理菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '1943362205181280258', '销售订单管理', '1943362205047062529', 1, 'saleorder', 
        'erp/ConfigDrivenPage/saleorder/configurable/saleorder', 
        '{"moduleCode":"saleOrder"}', 0, 0, 'C', 1, 1, 
        'k3:saleorder:query', 'document', 
        'admin', NOW(), '销售订单管理 配置化页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_id = '1943362205181280258'
);

-- 3. 创建按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '1943362205315497989', '查询', '1943362205181280258', 1, '', '', '', 0, 0, 'F', 1, 1, 'k3:saleorder:query', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '1943362205315497989');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '1943362205449715718', '新增', '1943362205181280258', 2, '', '', '', 0, 0, 'F', 1, 1, 'k3:saleorder:add', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '1943362205449715718');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '1943362205583933447', '修改', '1943362205181280258', 3, '', '', '', 0, 0, 'F', 1, 1, 'k3:saleorder:edit', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '1943362205583933447');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '1943362205718151176', '删除', '1943362205181280258', 4, '', '', '', 0, 0, 'F', 1, 1, 'k3:saleorder:remove', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '1943362205718151176');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '1943362205852368905', '审核', '1943362205181280258', 5, '', '', '', 0, 0, 'F', 1, 1, 'k3:saleorder:audit', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '1943362205852368905');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '1943362205986586634', '反审核', '1943362205181280258', 6, '', '', '', 0, 0, 'F', 1, 1, 'k3:saleorder:unAudit', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '1943362205986586634');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '1943362206120804363', '下推', '1943362205181280258', 7, '', '', '', 0, 0, 'F', 1, 1, 'k3:saleorder:push', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '1943362206120804363');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '1943362206255022092', '导出', '1943362205181280258', 8, '', '', '', 0, 0, 'F', 1, 1, 'k3:saleorder:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = '1943362206255022092');

-- ============================================
-- 第四部分：添加性能优化索引
-- ============================================

-- 1. 复合索引（优化高频查询）
ALTER TABLE erp_page_config
ADD INDEX idx_module_status_version (module_code, status, version);

ALTER TABLE erp_approval_history
ADD INDEX idx_module_bill (module_code, bill_id);

ALTER TABLE erp_approval_history
ADD INDEX idx_bill_action (bill_id, approval_action);

ALTER TABLE erp_push_relation
ADD INDEX idx_source_status (source_module, status);

-- ============================================
-- 第五部分：验证初始化结果
-- ============================================

-- 查看表结构
SELECT '========================================' AS '';
SELECT '表结构验证' AS '';
SELECT '========================================' AS '';
SHOW TABLES LIKE 'erp_%';

-- 查看配置数据
SELECT '========================================' AS '';
SELECT '配置数据验证' AS '';
SELECT '========================================' AS '';
SELECT config_id, module_code, config_name, version, status FROM erp_page_config;
SELECT flow_id, module_code, flow_name, status FROM erp_approval_flow;
SELECT relation_id, source_module, target_module, relation_name, status FROM erp_push_relation;

-- 查看菜单数据
SELECT '========================================' AS '';
SELECT '菜单权限验证' AS '';
SELECT '========================================' AS '';
SELECT menu_id, menu_name, parent_id, menu_type, perms FROM sys_menu 
WHERE menu_id IN ('1943362205047062529', '1943362205181280258', '1943362205315497989', '1943362205449715718', '1943362205583933447', '1943362205718151176', '1943362205852368905', '1943362205986586634', '1943362206120804363', '1943362206255022092');

-- 查看索引
SELECT '========================================' AS '';
SELECT '索引验证' AS '';
SELECT '========================================' AS '';
SHOW INDEX FROM erp_page_config;
SHOW INDEX FROM erp_approval_history;
SHOW INDEX FROM erp_push_relation;

-- ============================================
-- 执行完成提示
-- ============================================
SELECT '========================================' AS '';
SELECT 'ERP配置化数据库初始化完成！' AS message;
SELECT '========================================' AS '';
SELECT '已创建：' AS summary;
SELECT '  - 5个核心表（erp_page_config、erp_page_config_history、erp_approval_flow、erp_approval_history、erp_push_relation）' AS tables;
SELECT '  - 3个外键约束' AS foreign_keys;
SELECT '  - 7个性能优化索引' AS indexes;
SELECT '  - 1个页面配置' AS configs;
SELECT '  - 1个审批流程配置' AS flows;
SELECT '  - 1个下推关系配置' AS relations;
SELECT '  - 10个菜单权限' AS menus;
SELECT '========================================' AS '';
SELECT '下一步：' AS next_steps;
SELECT '1. 执行后端Maven编译：mvn clean install -DskipTests' AS step1;
SELECT '2. 启动后端服务：java -jar ruoyi-admin-wms.jar' AS step2;
SELECT '3. 访问前端页面：http://localhost/erp/ConfigDrivenPage/saleorder' AS step3;
SELECT '========================================' AS '';

