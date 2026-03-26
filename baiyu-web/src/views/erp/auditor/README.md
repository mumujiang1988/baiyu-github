# ERP配置审计系统

## 简介

ERP配置审计系统是一个用于验证ERP低代码配置文件完整性和规范性的工具。通过多个专业审计器对配置文件进行全面检查，确保配置的正确性、一致性和生产可行性。

## 功能特性

- ✅ **结构完整性审计** - 验证必需节点、节点属性、节点关联性
- ✅ **命名规范审计** - 验证字段命名、字典键命名规范性
- ✅ **API接口审计** - 验证接口路径、动态参数、接口依赖关系
- ✅ **前端渲染审计** - 验证组件配置、表格列渲染、表单字段验证
- ✅ **数据流审计** - 验证查询、表单、字典数据流完整性和闭环性
- ✅ **CRUD审计** - 验证查询构建器、CRUD操作配置
- ✅ **生产可行性审计** - 验证权限配置、性能可行性、数据源配置
- ✅ **多格式报告** - 支持JSON、Markdown、HTML格式输出

## 安装

```bash
# 安装依赖
npm install

# 全局安装（可选）
npm link
```

## 使用方法

### 命令行工具

```bash
# 审计单个配置文件
node cli.js <config-file> [options]

# 选项:
#   -f, --format <format>   输出格式 (json|markdown|html) (默认: "json")
#   -o, --output <path>     输出文件路径
#   --no-parallel           禁用并行执行
#   --timeout <ms>          审计器超时时间(毫秒) (默认: "30000")

# 示例
node cli.js business.config.json -f markdown -o report.md
node cli.js business.config.json -f html -o report.html
```

### 批量审计

```bash
# 批量审计目录下的所有配置文件
node cli.js batch <directory> [options]

# 选项:
#   -f, --format <format>   输出格式 (默认: "json")
#   -o, --output <path>     输出目录路径
#   -p, --pattern <pattern> 文件匹配模式 (默认: "**/*.config.json")

# 示例
node cli.js batch ./configs -f markdown -o ./reports
```

### 查看规则列表

```bash
# 显示所有审计规则
node cli.js rules
```

### 编程接口

```javascript
const { audit, auditAndReport, createAuditor } = require('./index');

// 方式1: 快速审计
const report = await audit('business.config.json');
console.log(report.summary);

// 方式2: 审计并生成报告文件
await auditAndReport(
  'business.config.json',  // 配置文件路径
  'report.md',             // 输出路径
  'markdown'               // 输出格式
);

// 方式3: 自定义审计器
const auditor = createAuditor('business.config.json', {
  parallel: true,
  timeout: 30000
});

// 注册自定义审计器
auditor.registerAuditor(new MyCustomAuditor());

// 执行审计
const report = await auditor.runAudit();

// 生成报告
await auditor.generateReport(report, 'html', 'report.html');
```

## 审计器说明

### 1. StructureAuditor - 结构完整性审计器

验证配置文件结构完整性，检查必需节点、节点属性、节点关联性。

**检查项:**
- 必需节点存在性 (pageConfig, apiConfig, businessConfig等)
- 必需字段完整性
- 搜索字段与查询条件一致性
- 表格列与表单字段映射
- 子表配置一致性
- API接口完整性

### 2. NamingAuditor - 命名规范审计器

验证字段命名、属性命名、字典键命名的规范性。

**检查项:**
- 字段命名规范 (金蝶K3、自定义、Oracle)
- 大小写一致性
- 字典键命名规范
- 字典value唯一性

### 3. ApiAuditor - API接口审计器

验证API接口路径规范性、接口依赖关系、动态参数可解析性。

**检查项:**
- API路径规范性
- RESTful规范
- 动态参数可解析性
- 接口依赖关系

### 4. RenderingAuditor - 前端渲染审计器

验证前端组件配置、表格列渲染、表单字段验证的可执行性。

**检查项:**
- 搜索组件配置完整性
- 表格列渲染配置
- 表单字段验证规则
- 操作按钮配置

### 5. DataFlowAuditor - 数据流审计器

验证查询数据流、表单数据流、字典数据流的完整性和闭环性。

**检查项:**
- 查询数据流完整性
- 子表数据流完整性
- 表单数据流完整性
- 字典数据流完整性

### 6. CrudAuditor - CRUD审计器

验证后端查询构建器、CRUD操作配置。

**检查项:**
- 查询构建器配置
- 主表CRUD配置
- 子表CRUD配置
- 数据库兼容性

### 7. FeasibilityAuditor - 生产可行性审计器

验证生产环境数据库兼容性、权限配置、性能可行性。

**检查项:**
- 权限配置完整性
- 性能可行性评估
- 数据源配置完整性

## 报告格式

### JSON格式

```json
{
  "configPath": "business.config.json",
  "auditTime": "2026-03-26T12:00:00.000Z",
  "summary": {
    "total": 100,
    "passed": 85,
    "failed": 15,
    "errors": 5,
    "warnings": 8,
    "infos": 2,
    "passRate": "85.00"
  },
  "results": [
    {
      "ruleId": "REQ-2.1.1",
      "severity": "error",
      "passed": false,
      "message": "缺少必需节点: pageConfig",
      "path": "$.pageConfig",
      "suggestion": "请添加 pageConfig 节点配置",
      "timestamp": "2026-03-26T12:00:00.000Z"
    }
  ]
}
```

### Markdown格式

```markdown
# ERP配置审计报告

## 📊 审计概览

- **配置文件**: business.config.json
- **审计时间**: 2026-03-26T12:00:00.000Z
- **总检查项**: 100
- **通过项**: 85
- **失败项**: 15
- **通过率**: 85.00%

## ❌ 严重错误 (阻塞部署)

| 规则ID | 消息 | 路径 | 建议 |
|--------|------|------|------|
| REQ-2.1.1 | 缺少必需节点: pageConfig | $.pageConfig | 请添加 pageConfig 节点配置 |
```

### HTML格式

生成可视化的HTML报告，支持折叠展开详细结果。

## CI/CD集成

### GitLab CI

```yaml
# .gitlab-ci.yml
audit:
  stage: test
  script:
    - node auditor/cli.js config/business.config.json -f json -o audit-report.json
  artifacts:
    paths:
      - audit-report.json
    expire_in: 1 week
```

### GitHub Actions

```yaml
# .github/workflows/audit.yml
name: Config Audit
on: [push, pull_request]

jobs:
  audit:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-node@v2
      - run: npm install
      - run: node auditor/cli.js config/business.config.json -f markdown -o audit-report.md
      - uses: actions/upload-artifact@v2
        with:
          name: audit-report
          path: audit-report.md
```

## 开发自定义审计器

```javascript
const { AuditResult } = require('./core/models/AuditResult');

class MyCustomAuditor {
  get name() {
    return 'MyCustomAuditor';
  }

  get description() {
    return '自定义审计器';
  }

  async audit(config) {
    const results = [];

    // 实现审计逻辑
    if (!config.myCustomField) {
      results.push(AuditResult.error(
        'CUSTOM-001',
        '缺少自定义字段',
        '$.myCustomField',
        '请添加 myCustomField 配置'
      ));
    }

    return results;
  }

  getRules() {
    return [
      { id: 'CUSTOM-001', name: '自定义字段检查', severity: 'error' }
    ];
  }
}

module.exports = MyCustomAuditor;
```

## 许可证

MIT
