-- ============================================
-- ERP 业务菜单 SQL - 销售订单管理
-- 创建时间：2026-03-25 11:41:41
-- 说明：为 销售订单管理 创建菜单和按钮权限（所有 ID 使用雪花算法）
-- ============================================

-- ============================================
-- 第一部分：创建菜单和按钮权限
-- ============================================

-- 1. 确保父菜单存在（ERP 业务菜单）- parent_id=0 表示根菜单，使用 COLLATE 确保字符集一致
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, remark, create_by, create_time)
SELECT '1943362205047062529', 'ERP 业务菜单' COLLATE utf8mb4_general_ci, 
       0,
       5, 'business', '', 0, 0, 'M', 1, 1, '', 'document', 
       'ERP 业务菜单目录', 'admin', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu 
    WHERE menu_name COLLATE utf8mb4_general_ci = 'ERP 业务菜单' COLLATE utf8mb4_general_ci
);

-- 获取父菜单 ID（根据 menu_name 查询，使用 COLLATE 确保字符集一致）
SET @parent_id := (SELECT menu_id FROM sys_menu WHERE menu_name COLLATE utf8mb4_general_ci = 'ERP 业务菜单' COLLATE utf8mb4_general_ci LIMIT 1);

-- 2. 创建子菜单：销售订单管理（根据 menu_name + parent_id 判断是否已存在，使用 COLLATE 确保字符集一致）
SET @menu_id := '1943362205181280258';
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @menu_id, '销售订单管理' COLLATE utf8mb4_general_ci, @parent_id, 1, 'saleorder', 
        'erp/ConfigDrivenPage/saleorder/configurable/saleorder', 
        '{"moduleCode":"saleorder"}', 0, 0, 'C', 1, 1, 
        'k3:saleorder:query', 'document', 
        'admin', NOW(), '', NULL, '销售订单管理 配置化页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu 
    WHERE menu_name COLLATE utf8mb4_general_ci = '销售订单管理' COLLATE utf8mb4_general_ci
    AND parent_id = @parent_id
);

-- 3. 创建按钮权限（每个按钮使用独立的雪花 ID，根据 menu_name + parent_id 判断是否已存在）
-- 先创建临时表存储按钮数据（显式指定字符集避免冲突）
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_buttons (
    menu_id VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    menu_name VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    parent_id BIGINT,
    order_num INT,
    perms VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    status CHAR(1) DEFAULT '1'
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 清空临时表
DELETE FROM tmp_buttons;

-- 插入按钮数据到临时表（status=1 表示正常启用）
INSERT INTO tmp_buttons (menu_id, menu_name, parent_id, order_num, perms, status) VALUES
('1943362205315497989', '查询', @menu_id, 1, 'k3:saleorder:query', '1'),
('1943362205449715718', '新增', @menu_id, 2, 'k3:saleorder:add', '1'),
('1943362205583933447', '修改', @menu_id, 3, 'k3:saleorder:edit', '1'),
('1943362205718151176', '删除', @menu_id, 4, 'k3:saleorder:remove', '1'),
('1943362205852368905', '审核', @menu_id, 5, 'k3:saleorder:audit', '1'),
('1943362205986586634', '反审核', @menu_id, 6, 'k3:saleorder:unAudit', '1'),
('1943362206120804363', '下推', @menu_id, 7, 'k3:saleorder:push', '1'),
('1943362206255022092', '导出', @menu_id, 8, 'k3:saleorder:export', '1');

-- 从临时表插入到 sys_menu，跳过已存在的记录（使用 COLLATE 确保字符集一致）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT t.menu_id, t.menu_name COLLATE utf8mb4_general_ci, @menu_id, t.order_num, '', '', '', 0, 0, 'F', 1, 1, t.perms COLLATE utf8mb4_general_ci, '#', 'admin', NOW(), '', NULL, ''
FROM tmp_buttons t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu s 
    WHERE s.menu_name COLLATE utf8mb4_general_ci = t.menu_name COLLATE utf8mb4_general_ci
    AND s.parent_id = @menu_id
);

-- 清理临时表
DROP TEMPORARY TABLE IF EXISTS tmp_buttons;

-- ============================================
-- 第二部分：导入页面配置数据
-- ============================================

-- 1. 创建触发器（如果不存在）
DELIMITER \$\$

CREATE TRIGGER IF NOT EXISTS trg_erp_config_history 
AFTER UPDATE ON erp_page_config 
FOR EACH ROW 
BEGIN
  INSERT INTO erp_page_config_history (
    config_id, module_code, config_type, version, config_content, 
    change_reason, change_type, create_by
  ) VALUES (
    NEW.config_id, NEW.module_code, NEW.config_type, NEW.version, NEW.config_content,
    CONCAT('从版本 ', OLD.version, ' 更新到版本 ', NEW.version),
    'UPDATE', NEW.update_by
  );
END\$\$

DELIMITER ;

-- 2. 插入页面配置
INSERT INTO erp_page_config (
  config_id, module_code, config_name, config_type, config_content, 
  version, status, is_public, remark, create_by, create_time
) VALUES (
  1943362206255022084,
  'saleorder',
  '销售订单管理 页面配置',
  'PAGE',
  '{"pageConfig":{"title":"销售订单管理","moduleCode":"saleorder","permissionPrefix":"k3:saleorder","apiPrefix":"/erp/engine","layout":"standard","primaryKey":"id","billNoField":"FBillNo"},"apiConfig":{"engineApis":{"query":"/erp/engine/query/execute","buildQuery":"/erp/engine/query/build","validation":"/erp/engine/validation/execute","approvalCurrentStep":"/erp/engine/approval/current-step","approvalExecute":"/erp/engine/approval/execute","approvalHistory":"/erp/engine/approval/history","approvalWorkflow":"/erp/engine/approval/workflow","approvalTransfer":"/erp/engine/approval/transfer","approvalWithdraw":"/erp/engine/approval/withdraw","pushTargets":"/erp/engine/push/targets","pushExecute":"/erp/engine/push/execute","pushPreview":"/erp/engine/push/preview","pushBatch":"/erp/engine/push/batch","pushMapping":"/erp/engine/push/mapping","pushValidate":"/erp/engine/push/validate","pushCancel":"/erp/engine/push/cancel","pushHistory":"/erp/engine/push/history"}},"businessConfig":{"entityName":"销售订单管理","entityNameSingular":"销售订单","dialogTitle":{"add":"新增{entityName}","edit":"修改{entityName}"},"drawerTitle":"{entityName}详情 - {billNo}","messages":{"selectOne":"请选择一条数据","confirmDelete":"是否确认删除选中的 {count} 条数据？","confirmAudit":"是否确认审核选中的 {count} 条数据？","confirmUnAudit":"是否确认反审核选中的 {count} 条数据？","success":{"add":"新增成功","edit":"修改成功","delete":"删除成功","audit":"审核成功","unAudit":"反审核成功"},"error":{"load":"加载数据失败","save":"保存失败","delete":"删除失败","audit":"审核失败","unAudit":"反审核失败"}}},"searchConfig":{"showSearch":true,"defaultExpand":true,"fields":[{"field":"FDate","label":"日期区间","component":"daterange","props":{"startPlaceholder":"开始日期","endPlaceholder":"结束日期","valueFormat":"YYYY-MM-DD","style":{"width":"240px"}},"defaultValue":"currentMonth","changeEvent":"handleQuery"},{"field":"FBillNo","label":"单据编号","component":"input","props":{"placeholder":"输入单据编号","clearable":true,"prefixIcon":"Search","style":{"width":"180px"}}},{"field":"F_ora_BaseProperty","label":"客户简称","component":"input","props":{"placeholder":"输入客户简称","clearable":true,"prefixIcon":"User","style":{"width":"150px"}}},{"field":"FSalerId","label":"销售员","component":"select","props":{"placeholder":"选择销售员","clearable":true,"filterable":true,"style":{"width":"120px"}},"dictionary":"salespersons","dataApi":"/erp/engine/dictionary/salespersons?moduleCode={moduleCode}","labelField":"nickName","valueField":"fseller","defaultValue":""},{"field":"orderStatus","label":"订单状态","component":"select","props":{"placeholder":"选择状态","clearable":true,"style":{"width":"100px"}},"options":[{"label":"未关闭","value":"A"},{"label":"已关闭","value":"B"},{"label":"业务终止","value":"C"}],"defaultValue":""},{"field":"FDocumentStatus","label":"单据状态","component":"select","props":{"placeholder":"选择单据状态","clearable":true,"filterable":true,"style":{"width":"120px"}},"dictionary":"documentStatus","defaultValue":""}]},"tableConfig":{"rowKey":"id","border":true,"stripe":true,"maxHeight":"calc(100vh - 380px)","showOverflowTooltip":true,"resizable":true,"columns":[{"type":"selection","width":55,"fixed":"left","resizable":false},{"type":"expand","width":100,"fixed":"left","resizable":false,"label":"详情"},{"prop":"FBillNo","label":"单据编号","width":150,"fixed":"left","align":"left","visible":true,"resizable":true,"renderType":"text"},{"prop":"F_ora_BaseProperty","label":"客户简称","width":150,"fixed":"left","align":"left","visible":true,"resizable":true},{"prop":"orderStatus","label":"订单状态","width":120,"align":"center","visible":true,"renderType":"tag","dictionary":"orderStatus"},{"prop":"FDocumentStatus","label":"单据状态","width":140,"align":"center","visible":true,"renderType":"tag","dictionary":"documentStatus"},{"prop":"FDate","label":"销售合同日期","width":140,"align":"center","visible":true,"renderType":"date","format":"YYYY-MM-DD"},{"prop":"FSalerId","label":"销售员","width":120,"align":"left","visible":true,"renderType":"text","dictionary":"salespersons"},{"prop":"FBillAmount","label":"订单金额","width":140,"align":"right","visible":true,"renderType":"currency","precision":2},{"prop":"FBillTaxAmount","label":"税额","width":100,"align":"right","visible":true,"renderType":"currency","precision":2},{"prop":"FSettleCurrId","label":"结算币别","width":140,"align":"center","visible":true,"renderType":"text","dictionary":"currency"},{"prop":"FCreateDate","label":"创建时间","width":160,"align":"center","visible":true,"renderType":"datetime","format":"YYYY-MM-DD HH:mm:ss"},{"prop":"FCreatorId","label":"创建人","width":100,"align":"left","visible":true,"renderType":"text"}],"expandRow":{"enabled":true,"trigger":"hover","loadStrategy":"lazy","tabs":[{"name":"entry","label":"销售订单明细","dataField":"entryList","api":"/erp/engine/custom/entry?moduleCode={moduleCode}\\\\u0026billNo={billNo}","table":{"columns":[{"prop":"fPlanMaterialId","label":"物料编码","width":120},{"prop":"fPlanMaterialName","label":"物料名称","width":150,"showOverflowTooltip":true},{"prop":"fQty","label":"数量","width":100,"renderType":"number"},{"prop":"fPrice","label":"单价","width":100,"renderType":"currency"},{"prop":"fAllAmount","label":"金额合计","width":120,"renderType":"currency"},{"prop":"fDeliQty","label":"已交付数量","width":100},{"prop":"f_mz","label":"毛重","width":80,"align":"right","renderType":"number"},{"prop":"f_jz","label":"净重","width":80,"align":"right","renderType":"number"},{"prop":"f_cplb","label":"产品类别","width":100,"align":"center"}]}},{"name":"cost","label":"成本暂估","dataField":"costData","api":"/erp/engine/custom/cost?moduleCode={moduleCode}\\\\u0026billNo={billNo}","type":"descriptions","columns":3,"fields":[{"prop":"fHyf","label":"海运费（外币）","renderType":"currency"},{"prop":"fBillAllAmount","label":"价税合计","renderType":"currency"},{"prop":"fJlre","label":"净利润额","renderType":"currency"},{"prop":"fLrl","label":"毛净利润率%","renderType":"percent"},{"prop":"fJlrl","label":"净利润率%","renderType":"percent"}]}]}},"drawerConfig":{"enabled":true,"trigger":"click","loadStrategy":"lazy","title":"销售订单详情 - {billNo}","tabs":[{"name":"entry","label":"销售订单明细","dataField":"entryList","api":"/erp/engine/custom/entry?moduleCode={moduleCode}\\\\u0026billNo={billNo}","type":"table","table":{"columns":[{"prop":"fPlanMaterialId","label":"物料编码","width":120,"align":"center"},{"prop":"fPlanMaterialName","label":"物料名称","width":180,"showOverflowTooltip":true,"align":"left"},{"prop":"fQty","label":"数量","width":100,"renderType":"number","align":"right"},{"prop":"fPrice","label":"单价","width":100,"renderType":"currency","align":"right"},{"prop":"fTaxPrice","label":"含税单价","width":100,"renderType":"currency","align":"right"},{"prop":"fAllAmount","label":"金额合计","width":120,"renderType":"currency","align":"right"},{"prop":"fDeliQty","label":"已交付数量","width":100,"renderType":"number","align":"right"},{"prop":"f_mz","label":"毛重","width":80,"renderType":"number","align":"right"},{"prop":"f_jz","label":"净重","width":80,"renderType":"number","align":"right"},{"prop":"f_kpdj","label":"开票单价","width":100,"renderType":"currency","align":"right"},{"prop":"f_ygcb","label":"预估成本","width":100,"renderType":"currency","align":"right"},{"prop":"f_hsbm","label":"海关编码","width":100,"align":"center"},{"prop":"f_cplb","label":"产品类别","width":100,"align":"center"}]}},{"name":"cost","label":"成本暂估","dataField":"costData","api":"/erp/engine/custom/cost?moduleCode={moduleCode}\\\\u0026billNo={billNo}","type":"descriptions","columns":3,"fields":[{"prop":"fHyf","label":"海运费 (外币)","renderType":"currency"},{"prop":"fBillAllAmount","label":"价税合计","renderType":"currency"},{"prop":"fBillAllAmountLc","label":"价税合计 (本位币)","renderType":"currency"},{"prop":"fBxf","label":"保险费","renderType":"currency"},{"prop":"fGwyhfy","label":"国外银行费用","renderType":"currency"},{"prop":"fQtwbfy","label":"其他外币费用","renderType":"currency"},{"prop":"fMxcbhj","label":"明细成本合计","renderType":"currency"},{"prop":"fMxtshj","label":"明细退税合计","renderType":"currency"},{"prop":"fCbxj","label":"成本小计 RMB","renderType":"currency"},{"prop":"fBzf","label":"包装费","renderType":"currency"},{"prop":"fDlf","label":"代理费","renderType":"currency"},{"prop":"fRzf","label":"认证费","renderType":"currency"},{"prop":"fKdf","label":"快递费成本","renderType":"currency"},{"prop":"fHdf","label":"货贷费","renderType":"currency"},{"prop":"fLyf","label":"陆运费","renderType":"currency"},{"prop":"fQtfy","label":"其他费用","renderType":"currency"},{"prop":"fMjf","label":"模具费","renderType":"currency"},{"prop":"fJcf","label":"进仓费","renderType":"currency"},{"prop":"fFyxj","label":"费用小计","renderType":"currency"},{"prop":"fWbyk","label":"外币盈亏","renderType":"currency"},{"prop":"fJlre","label":"净利润额","renderType":"currency"},{"prop":"fLrl","label":"毛净利润率%","renderType":"percent"},{"prop":"fJlrl","label":"净利润率%","renderType":"percent"}]}]},"formConfig":{"dialogWidth":"1400px","labelWidth":"120px","sections":[{"title":"基本信息","icon":"Document","columns":4,"fields":[{"field":"fbillno","label":"单据编号","component":"input","span":6,"required":true,"rules":[{"required":true,"message":"单据编号不能为空","trigger":"blur"}],"props":{"maxlength":100,"clearable":true}},{"field":"fdate","label":"销售合同日期","component":"date","span":6,"required":true,"rules":[{"required":true,"message":"销售合同日期不能为空","trigger":"change"}],"props":{"placeholder":"选择日期","valueFormat":"YYYY-MM-DD"}},{"field":"fcustid","label":"客户编码","component":"input","span":6,"required":true,"props":{"placeholder":"请输入客户编码","maxlength":100},"defaultValue":""},{"field":"f_ora_baseproperty","label":"客户简称","component":"input","span":6,"required":true,"rules":[{"required":true,"message":"客户简称不能为空","trigger":"blur"}],"props":{"placeholder":"请输入客户简称","maxlength":200},"defaultValue":""},{"field":"f_khhth","label":"客户合同号","component":"input","span":6,"props":{"maxlength":100,"clearable":true}},{"field":"f_kglxr","label":"客户联系人","component":"input","span":6,"props":{"maxlength":100,"clearable":true,"readonly":true}},{"field":"f_cty_baseproperty1","label":"客户邮箱","component":"input","span":6,"rules":[{"type":"email","message":"请输入正确的邮箱地址","trigger":["blur","change"]}],"props":{"maxlength":100,"clearable":true,"readonly":true}},{"field":"fsettlecurrid","label":"结算币别","comment":"用于与金蝶 K3 系统集成的结算币种标识","component":"select","span":6,"dictionary":"currency","props":{"placeholder":"请选择结算币别","clearable":true,"filterable":true},"defaultValue":"人民币"}]},{"title":"财务信息","icon":"Money","columns":4,"fields":[{"field":"f_tcbl","label":"提成比例","component":"input-number","span":6,"componentProps":{"min":0,"max":100,"precision":2,"step":0.1,"controlsPosition":"right"}},{"field":"fisincludedtax","label":"是否含税","component":"select","span":6,"options":[{"label":"是","value":1},{"label":"否","value":0}],"componentProps":{"placeholder":"请选择是否含税","clearable":true},"defaultValue":1},{"field":"f_sfbg","label":"是否报关","component":"select","span":6,"options":[{"label":"是","value":1},{"label":"否","value":0}],"componentProps":{"placeholder":"请选择是否报关","clearable":true},"defaultValue":1},{"field":"frecconditionid","label":"收款条件","component":"select","span":6,"dictionary":"paymentTerms","componentProps":{"placeholder":"请选择收款条件","clearable":true,"filterable":true}},{"field":"fbillamount","label":"订单金额","component":"input-number","span":6,"componentProps":{"min":0,"precision":2,"step":0.01,"controlsPosition":"right"}},{"field":"fbilltaxamount","label":"税额","component":"input-number","span":6,"componentProps":{"min":0,"precision":2,"step":0.01,"controlsPosition":"right"}},{"field":"flocalcurrid","label":"本位币","comment":"公司内部核算使用的本位货币，区别于结算币别","component":"select","span":6,"dictionary":"currency","componentProps":{"placeholder":"请选择本位币","clearable":true,"filterable":true}}]},{"title":"销售信息","icon":"ShoppingCart","columns":4,"fields":[{"field":"fsalerid","label":"销售员","component":"select","span":6,"dictionary":"salespersons","componentProps":{"placeholder":"请选择销售员","clearable":true,"filterable":true}},{"field":"f_lrl","label":"毛净利润率","component":"input-number","span":6,"componentProps":{"min":0,"max":100,"precision":2,"step":0.1,"controlsPosition":"right"}},{"field":"f_jlrl","label":"净利润率","component":"input-number","span":6,"componentProps":{"min":0,"max":100,"precision":2,"step":0.1,"controlsPosition":"right"}},{"field":"fstate","label":"国家","component":"select","span":6,"dictionary":"nation","componentProps":{"placeholder":"请选择国家","clearable":true,"filterable":true},"defaultValue":""},{"field":"f_gj","label":"抵运国家","component":"select","span":6,"dictionary":"nation","componentProps":{"placeholder":"请选择抵运国家","clearable":true,"filterable":true},"defaultValue":""},{"field":"f_myfs","label":"贸易方式","component":"select","span":6,"dictionary":"tradeType","componentProps":{"placeholder":"请选择贸易方式","clearable":true,"filterable":true},"defaultValue":"一般贸易"}]}],"formTabs":{"enabled":true,"tabs":[{"name":"entry","label":"销售订单明细","icon":"Document","table":{"addRow":true,"deleteRow":true,"columns":[{"prop":"fPlanMaterialId","label":"物料编码","width":120,"editable":true,"required":true},{"prop":"fPlanMaterialName","label":"物料名称","width":180,"editable":true,"showOverflowTooltip":true},{"prop":"fQty","label":"数量","width":100,"editable":true,"type":"number","required":true},{"prop":"fPrice","label":"单价","width":100,"editable":true,"type":"number"},{"prop":"fTaxPrice","label":"含税单价","width":100,"editable":true,"type":"number"},{"prop":"fAllAmount","label":"金额合计","width":120,"editable":true,"type":"number"},{"prop":"fDeliQty","label":"已交付数量","width":100,"editable":true,"type":"number"},{"prop":"f_mz","label":"毛重","width":80,"editable":true,"type":"number"},{"prop":"f_jz","label":"净重","width":80,"editable":true,"type":"number"},{"prop":"f_kpdj","label":"开票单价","width":100,"editable":true,"type":"number"},{"prop":"f_ygcb","label":"预估成本","width":100,"editable":true,"type":"number"},{"prop":"f_hsbm","label":"海关编码","width":100,"editable":true},{"prop":"f_cplb","label":"产品类别","width":100,"editable":true}]}},{"name":"cost","label":"成本暂估","icon":"Money","type":"form","columns":4,"fields":[{"field":"fHyf","label":"海运费 (外币)","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fBillAllAmount","label":"价税合计","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fBillAllAmountLc","label":"价税合计 (本位币)","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fBxf","label":"保险费","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fGwyhfy","label":"国外银行费用","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fQtwbfy","label":"其他外币费用","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fMxcbhj","label":"明细成本合计","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fMxtshj","label":"明细退税合计","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fCbxj","label":"成本小计 RMB","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fBzf","label":"包装费","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fDlf","label":"代理费","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fRzf","label":"认证费","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fKdf","label":"快递费成本","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fHdf","label":"货贷费","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fLyf","label":"陆运费","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fQtfy","label":"其他费用","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fMjf","label":"模具费","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fJcf","label":"进仓费","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fFyxj","label":"费用小计","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fWbyk","label":"外币盈亏","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fJlre","label":"净利润额","component":"input-number","span":6,"props":{"min":0,"precision":2,"step":0.01}},{"field":"fLrl","label":"毛净利润率%","component":"input-number","span":6,"props":{"min":0,"max":100,"precision":2,"step":0.1}},{"field":"fJlrl","label":"净利润率%","component":"input-number","span":6,"props":{"min":0,"max":100,"precision":2,"step":0.1}}]}]}},"actionConfig":{"toolbar":[{"type":"primary","label":"新增","icon":"Plus","permission":"k3:{moduleCode}:add","handler":"handleAdd","position":"left"},{"type":"success","label":"修改","icon":"Edit","permission":"k3:{moduleCode}:edit","handler":"handleUpdate","disabled":"single","position":"left"},{"type":"danger","label":"删除","icon":"Delete","permission":"k3:{moduleCode}:delete","handler":"handleDelete","disabled":"multiple","position":"left"},{"type":"success","label":"审核","icon":"CircleCheck","permission":"k3:{moduleCode}:audit","handler":"handleAudit","disabled":"multiple","position":"left"},{"type":"warning","label":"反审核","icon":"Close","permission":"k3:{moduleCode}:unAudit","handler":"handleUnAudit","disabled":"multiple","position":"left"},{"type":"info","label":"下推","icon":"Download","permission":"k3:{moduleCode}:push","handler":"handleOpenPushDialog","disabled":"single","position":"left"},{"type":"warning","label":"导出","icon":"Download","permission":"k3:{moduleCode}:export","handler":"handleExport","disabled":"multiple","position":"left"},{"type":"info","label":"列设置","icon":"Setting","handler":"openColumnSetting","position":"right"}],"row":[]},"dictionaryConfig":{"salespersons":{"api":"/erp/engine/dictionary/salespersons?moduleCode={moduleCode}","labelField":"nickName","valueField":"fseller"},"currency":{"api":"/erp/engine/dictionary/listByType/currency?type=currency\\\\u0026moduleCode={moduleCode}","labelField":"label","valueField":"value"},"paymentTerms":{"api":"/erp/engine/dictionary/listByType/payment_clause?type=payment_clause\\\\u0026moduleCode={moduleCode}","labelField":"label","valueField":"value"},"nation":{"api":"/erp/engine/dictionary/listByType/nation?type=nation\\\\u0026moduleCode={moduleCode}","labelField":"label","valueField":"value"},"tradeType":{"api":"/erp/engine/dictionary/listByType/trade_way?type=trade_way\\\\u0026moduleCode={moduleCode}","labelField":"label","valueField":"value"},"orderStatus":[{"label":"未关闭","value":"A","type":"success"},{"label":"已关闭","value":"B","type":"info"},{"label":"业务终止","value":"C","type":"danger"}],"documentStatus":[{"label":"暂存","value":"Z","type":"info"},{"label":"创建","value":"A","type":"success"},{"label":"审核中","value":"B","type":"warning"},{"label":"已审核","value":"C","type":"success"},{"label":"重新审核","value":"D","type":"primary"}]},"virtualFieldConfig":{"enabled":false,"fields":[]}}',
  1,
  '1',
  '0',
  '销售订单管理 配置化页面初始配置',
  'admin',
  NOW()
)
ON DUPLICATE KEY UPDATE
  config_name = VALUES(config_name),
  config_content = VALUES(config_content),
  version = version + 1,
  update_by = VALUES(create_by),
  update_time = VALUES(create_time);

-- ============================================
-- 完成提示
-- ============================================
SELECT '✅ 销售订单管理 菜单创建完成！' AS message;
SELECT '父菜单 ID: 1943362205047062529' AS parent_menu_info;
SELECT CONCAT('子菜单 ID: ', CAST(@menu_id AS CHAR)) AS menu_info;
SELECT CONCAT('包含按钮：', GROUP_CONCAT(menu_name ORDER BY order_num)) AS buttons
FROM sys_menu WHERE parent_id = @menu_id;
SELECT '✅ 销售订单管理 配置数据导入成功！' AS message;
SELECT config_id, module_code, config_name, version FROM erp_page_config 
WHERE module_code = 'saleorder';
