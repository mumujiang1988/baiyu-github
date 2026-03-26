# ERP配置审计报告

## 📊 审计概览

- **配置文件**: D:\baiyuyunma\baiyu-github\baiyu-github\baiyu-web\src\views\erp\pageTemplate\configs\business.config.template.json
- **审计时间**: 2026-03-26T03:26:50.578Z
- **总检查项**: 57
- **通过项**: 26
- **失败项**: 31
- **通过率**: 45.61%

## ❌ 严重错误 (阻塞部署)

| 规则ID | 消息 | 路径 | 建议 |
|--------|------|------|------|
| AUDITOR-ERROR | 审计器执行失败: subTableConfigs.forEach is not a function |  | 请检查审计器实现或配置文件格式 |
| AUDITOR-ERROR | 审计器执行失败: config.subTableQueryConfigs.forEach is not a function |  | 请检查审计器实现或配置文件格式 |
| AUDITOR-ERROR | 审计器执行失败: config.subTableQueryConfigs.forEach is not a function |  | 请检查审计器实现或配置文件格式 |
| AUDITOR-ERROR | 审计器执行失败: config.subTableQueryConfigs.forEach is not a function |  | 请检查审计器实现或配置文件格式 |
| AUDITOR-ERROR | 审计器执行失败: config.subTableQueryConfigs.forEach is not a function |  | 请检查审计器实现或配置文件格式 |
| REQ-3.1.3 | 字段 FDate, fdate 大小写不一致 | $.searchConfig.fields[0]
$.tableConfig.columns[6]
$.formConfig.sections[0].fields[1] | 请统一使用同一种大小写形式，建议: FDate |
| REQ-3.1.3 | 字段 FBillNo, fbillno 大小写不一致 | $.searchConfig.fields[1]
$.tableConfig.columns[2]
$.formConfig.sections[0].fields[0] | 请统一使用同一种大小写形式，建议: FBillNo |
| REQ-3.1.3 | 字段 F_ora_BaseProperty, f_ora_baseproperty 大小写不一致 | $.searchConfig.fields[2]
$.tableConfig.columns[3]
$.formConfig.sections[0].fields[3] | 请统一使用同一种大小写形式，建议: F_ora_BaseProperty |
| REQ-3.1.3 | 字段 FSalerId, fsalerid 大小写不一致 | $.searchConfig.fields[3]
$.tableConfig.columns[7]
$.formConfig.sections[2].fields[0] | 请统一使用同一种大小写形式，建议: FSalerId |
| REQ-3.1.3 | 字段 FBillAmount, fbillamount 大小写不一致 | $.tableConfig.columns[8]
$.formConfig.sections[1].fields[4] | 请统一使用同一种大小写形式，建议: FBillAmount |
| REQ-3.1.3 | 字段 FBillTaxAmount, fbilltaxamount 大小写不一致 | $.tableConfig.columns[9]
$.formConfig.sections[1].fields[5] | 请统一使用同一种大小写形式，建议: FBillTaxAmount |
| REQ-3.1.3 | 字段 FSettleCurrId, fsettlecurrid 大小写不一致 | $.tableConfig.columns[10]
$.formConfig.sections[0].fields[7] | 请统一使用同一种大小写形式，建议: FSettleCurrId |
| REQ-5.2.2 | tag渲染列 orderStatus 引用的字典 orderStatus 不存在 | $.tableConfig.columns[4] | 请在 dictionaryConfig 中定义 orderStatus 字典 |
| REQ-5.2.2 | tag渲染列 FDocumentStatus 引用的字典 documentStatus 不存在 | $.tableConfig.columns[5] | 请在 dictionaryConfig 中定义 documentStatus 字典 |

## ⚠️ 警告 (建议修复)

| 规则ID | 消息 | 路径 | 建议 |
|--------|------|------|------|
| REQ-3.1.1 | 字段 F_ora_BaseProperty 不符合任何命名规范 | $.searchConfig.fields[2] | 建议使用以下规范之一:
  - 金蝶K3: FBillNo, FQty, FAmount
  - 自定义: f_bill_no, f_qty, f_amount
  - Oracle: F_ora_bill_no, F_ora_qty |
| REQ-3.1.1 | 字段 orderStatus 不符合任何命名规范 | $.searchConfig.fields[4] | 建议使用以下规范之一:
  - 金蝶K3: FBillNo, FQty, FAmount
  - 自定义: f_bill_no, f_qty, f_amount
  - Oracle: F_ora_bill_no, F_ora_qty |
| REQ-3.1.1 | 字段 fbillno 不符合任何命名规范 | $.formConfig.sections[0].fields[0] | 建议使用以下规范之一:
  - 金蝶K3: FBillNo, FQty, FAmount
  - 自定义: f_bill_no, f_qty, f_amount
  - Oracle: F_ora_bill_no, F_ora_qty |
| REQ-3.1.1 | 字段 fdate 不符合任何命名规范 | $.formConfig.sections[0].fields[1] | 建议使用以下规范之一:
  - 金蝶K3: FBillNo, FQty, FAmount
  - 自定义: f_bill_no, f_qty, f_amount
  - Oracle: F_ora_bill_no, F_ora_qty |
| REQ-3.1.1 | 字段 fcustid 不符合任何命名规范 | $.formConfig.sections[0].fields[2] | 建议使用以下规范之一:
  - 金蝶K3: FBillNo, FQty, FAmount
  - 自定义: f_bill_no, f_qty, f_amount
  - Oracle: F_ora_bill_no, F_ora_qty |
| REQ-3.1.1 | 字段 fsettlecurrid 不符合任何命名规范 | $.formConfig.sections[0].fields[7] | 建议使用以下规范之一:
  - 金蝶K3: FBillNo, FQty, FAmount
  - 自定义: f_bill_no, f_qty, f_amount
  - Oracle: F_ora_bill_no, F_ora_qty |
| REQ-3.1.1 | 字段 fisincludedtax 不符合任何命名规范 | $.formConfig.sections[1].fields[1] | 建议使用以下规范之一:
  - 金蝶K3: FBillNo, FQty, FAmount
  - 自定义: f_bill_no, f_qty, f_amount
  - Oracle: F_ora_bill_no, F_ora_qty |
| REQ-3.1.1 | 字段 frecconditionid 不符合任何命名规范 | $.formConfig.sections[1].fields[3] | 建议使用以下规范之一:
  - 金蝶K3: FBillNo, FQty, FAmount
  - 自定义: f_bill_no, f_qty, f_amount
  - Oracle: F_ora_bill_no, F_ora_qty |
| REQ-3.1.1 | 字段 fbillamount 不符合任何命名规范 | $.formConfig.sections[1].fields[4] | 建议使用以下规范之一:
  - 金蝶K3: FBillNo, FQty, FAmount
  - 自定义: f_bill_no, f_qty, f_amount
  - Oracle: F_ora_bill_no, F_ora_qty |
| REQ-3.1.1 | 字段 fbilltaxamount 不符合任何命名规范 | $.formConfig.sections[1].fields[5] | 建议使用以下规范之一:
  - 金蝶K3: FBillNo, FQty, FAmount
  - 自定义: f_bill_no, f_qty, f_amount
  - Oracle: F_ora_bill_no, F_ora_qty |
| REQ-3.1.1 | 字段 flocalcurrid 不符合任何命名规范 | $.formConfig.sections[1].fields[6] | 建议使用以下规范之一:
  - 金蝶K3: FBillNo, FQty, FAmount
  - 自定义: f_bill_no, f_qty, f_amount
  - Oracle: F_ora_bill_no, F_ora_qty |
| REQ-3.1.1 | 字段 fsalerid 不符合任何命名规范 | $.formConfig.sections[2].fields[0] | 建议使用以下规范之一:
  - 金蝶K3: FBillNo, FQty, FAmount
  - 自定义: f_bill_no, f_qty, f_amount
  - Oracle: F_ora_bill_no, F_ora_qty |
| REQ-3.1.1 | 字段 fstate 不符合任何命名规范 | $.formConfig.sections[2].fields[3] | 建议使用以下规范之一:
  - 金蝶K3: FBillNo, FQty, FAmount
  - 自定义: f_bill_no, f_qty, f_amount
  - Oracle: F_ora_bill_no, F_ora_qty |
| REQ-5.1.1 | daterange组件缺少placeholder配置 | $.searchConfig.fields[0] | 建议配置placeholder提升用户体验，如: ["开始日期", "结束日期"] |
| REQ-5.1.2 | daterange组件缺少valueFormat配置 | $.searchConfig.fields[0] | 建议配置valueFormat，如: "YYYY-MM-DD" |
| REQ-5.3.2 | 必填字段 undefined 未配置验证规则 | $.formConfig.sections[0].fields[2] | 建议添加required验证规则 |
| REQ-5.3.4 | 字段 undefined 的验证规则使用了无效的trigger: blur,change | $.formConfig.sections[0].fields[6].rules[0] | 请使用有效的trigger: blur, change |

## 💡 优化建议

- **REQ-3.1.2**: 字段 FDate 符合 kingdee 命名规范
- **REQ-3.1.2**: 字段 FBillNo 符合 kingdee 命名规范
- **REQ-3.1.2**: 字段 FSalerId 符合 kingdee 命名规范
- **REQ-3.1.2**: 字段 FDocumentStatus 符合 kingdee 命名规范
- **REQ-3.1.2**: 字段 FBillAmount 符合 kingdee 命名规范
- **REQ-3.1.2**: 字段 FBillTaxAmount 符合 kingdee 命名规范
- **REQ-3.1.2**: 字段 FSettleCurrId 符合 kingdee 命名规范
- **REQ-3.1.2**: 字段 FCreateDate 符合 kingdee 命名规范
- **REQ-3.1.2**: 字段 FCreatorId 符合 kingdee 命名规范
- **REQ-3.1.2**: 字段 f_ora_baseproperty 符合 custom 命名规范
- **REQ-3.1.2**: 字段 f_khhth 符合 custom 命名规范
- **REQ-3.1.2**: 字段 f_kglxr 符合 custom 命名规范
- **REQ-3.1.2**: 字段 f_cty_baseproperty1 符合 custom 命名规范
- **REQ-3.1.2**: 字段 f_tcbl 符合 custom 命名规范
- **REQ-3.1.2**: 字段 f_sfbg 符合 custom 命名规范
- **REQ-3.1.2**: 字段 f_lrl 符合 custom 命名规范
- **REQ-3.1.2**: 字段 f_jlrl 符合 custom 命名规范
- **REQ-3.1.2**: 字段 f_gj 符合 custom 命名规范
- **REQ-3.1.2**: 字段 f_myfs 符合 custom 命名规范
- **REQ-5.1.4**: input组件未配置placeholder
  - 建议: 建议配置placeholder提升用户体验
- **REQ-5.1.4**: input组件未配置placeholder
  - 建议: 建议配置placeholder提升用户体验
- **REQ-5.3.3**: input-number字段 undefined 未配置数值范围
  - 建议: 建议配置min和max约束数值范围
- **REQ-5.3.3**: input-number字段 undefined 未配置数值范围
  - 建议: 建议配置min和max约束数值范围
- **REQ-5.3.3**: input-number字段 undefined 未配置数值范围
  - 建议: 建议配置min和max约束数值范围
- **REQ-5.3.3**: input-number字段 undefined 未配置数值范围
  - 建议: 建议配置min和max约束数值范围
- **REQ-5.3.3**: input-number字段 undefined 未配置数值范围
  - 建议: 建议配置min和max约束数值范围
