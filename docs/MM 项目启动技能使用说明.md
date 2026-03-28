# MM 项目启动技能使用说明

## 🚀 快速开始

### 一键自动启动（推荐）

```powershell
cd D:\baiyuyunma\baiyu-github\baiyu-github
.\scripts\start-project.ps1
```

**这就是全部！无需任何确认，脚本会自动完成：**
1. ✅ 停止旧的 Java/Node 进程
2. ✅ 检查开发环境（Maven、Java、Node、npm）
3. ✅ 清理并编译后端代码
4. ✅ 后台启动后端服务（新窗口）
5. ✅ 后台启动前端服务（新窗口）
6. ✅ 验证端口连通性
7. ✅ 显示访问地址

---

## 💡 常用参数

### 1. 跳过编译（已编译时使用）

```powershell
.\scripts\start-project.ps1 -SkipCompile
```

**适用场景**：
- 刚刚编译过，只是想重启服务
- 代码没有变化，只是需要重启
- 节省时间（可节省约 90 秒编译时间）

---

### 2. 仅启动后端

```powershell
.\scripts\start-project.ps1 -SkipFrontend
```

**适用场景**：
- 只需要后端 API 服务
- 前端已经在其他终端运行
- 调试后端接口

---

### 3. 查看帮助信息

```powershell
.\scripts\start-project.ps1 -Help
```

**输出示例**：
```
==================================
RuoYi-WMS 项目一键启动脚本
==================================

用法：.\start-project.ps1 [参数]

参数:
  -SkipCompile    跳过编译步骤（适用于已编译情况）
  -SkipFrontend   仅启动后端，不启动前端
  -Help           显示此帮助信息

示例:
  .\start-project.ps1                    # 完整启动流程
  .\start-project.ps1 -SkipCompile       # 跳过编译
  .\start-project.ps1 -SkipFrontend      # 仅启动后端
```

---

## 📋 完整启动流程示例

### 首次启动（需要编译）

```powershell
PS D:\baiyuyunma\baiyu-github\baiyu-github> .\scripts\start-project.ps1

==================================
RuoYi-WMS 项目一键启动
==================================
开始时间：2026-03-28 10:50:00

[阶段 1/5] 停止旧进程...
  ✓ 已停止所有 Java 进程
  ✓ 已停止所有 Node 进程

[阶段 2/5] 环境检查...
  ✓ Maven 已安装
  ✓ Java 已安装
  ✓ Node 已安装
  ✓ npm 已安装

[阶段 3/5] 清理并编译后端...
  ✓ Maven 编译成功 (耗时：90.5 秒)

[阶段 4/5] 启动后端服务...
  ✓ 后端启动进程已创建 (PID: 12345)
  等待后端初始化...
  ✓ 后端服务已就绪 (端口 8180)

[阶段 5/5] 启动前端服务...
  ✓ 前端启动进程已创建 (PID: 12346)
  等待前端初始化...
  ✓ 前端服务已就绪 (端口 8899)

==================================
项目启动完成！
==================================
结束时间：2026-03-28 10:52:15

访问地址:
  前端：http://localhost:8899/
  后端：http://localhost:8180/

提示:
  - 两个新窗口分别运行着后端和前端服务
  - 关闭窗口可停止对应服务
  - 如需重启，直接运行 .\scripts\start-project.ps1 即可
```

---

### 快速重启（已编译）

```powershell
PS D:\baiyuyunma\baiyu-github\baiyu-github> .\scripts\start-project.ps1 -SkipCompile

==================================
RuoYi-WMS 项目一键启动
==================================
开始时间：2026-03-28 11:00:00

[阶段 1/5] 停止旧进程...
  ✓ 已停止所有 Java 进程
  ✓ 已停止所有 Node 进程

[阶段 2/5] 环境检查...
  ✓ Maven 已安装
  ✓ Java 已安装
  ✓ Node 已安装
  ✓ npm 已安装

[阶段 3/5] 跳过编译（使用已有构建）

[阶段 4/5] 启动后端服务...
  ✓ 后端启动进程已创建 (PID: 12347)
  ✓ 后端服务已就绪 (端口 8180)

[阶段 5/5] 启动前端服务...
  ✓ 前端启动进程已创建 (PID: 12348)
  ✓ 前端服务已就绪 (端口 8899)

==================================
项目启动完成！
==================================
结束时间：2026-03-28 11:01:20

访问地址:
  前端：http://localhost:8899/
  后端：http://localhost:8180/
```

**总耗时仅约 80 秒（节省了 90 秒编译时间）**

---

## 🔧 故障处理

### 问题 1：编译失败

**错误提示**：
```
✗ Maven 编译失败
```

**解决方案**：
```powershell
# 1. 查看详细错误
mvn clean install -e

# 2. 强制更新依赖后重试
mvn clean install -U -DskipTests

# 3. 再次尝试启动
.\scripts\start-project.ps1
```

---

### 问题 2：端口被占用

**错误提示**：
```
⚠ 后端端口检测失败，可能仍在启动中...
```

**解决方案**：
```powershell
# 1. 检查端口占用
netstat -ano | findstr :8180

# 2. 强制停止所有 Java 进程
taskkill /f /im java.exe

# 3. 等待 3 秒后重新启动
Start-Sleep -Seconds 3
.\scripts\start-project.ps1 -SkipCompile
```

---

### 问题 3：前端启动失败

**错误提示**：
```
✗ 前端启动失败
```

**解决方案**：
```powershell
# 1. 检查 node_modules
cd D:\baiyuyunma\baiyu-github\baiyu-github\baiyu-web
npm install

# 2. 清除缓存
npm cache clean --force

# 3. 重新启动
.\scripts\start-project.ps1 -SkipCompile
```

---

## ⚡ 性能优化建议

### 1. 日常开发流程

**早上启动**（首次需要编译）：
```powershell
.\scripts\start-project.ps1
# 耗时：约 2 分钟（含编译 90 秒）
```

**中午休息后重启**（已编译）：
```powershell
.\scripts\start-project.ps1 -SkipCompile
# 耗时：约 80 秒
```

**下午继续**（已编译）：
```powershell
.\scripts\start-project.ps1 -SkipCompile
# 耗时：约 80 秒
```

---

### 2. 仅调试后端

```powershell
.\scripts\start-project.ps1 -SkipFrontend -SkipCompile
# 耗时：约 60 秒（仅启动后端）
```

---

### 3. 仅调试前端

```powershell
# 先手动启动后端（如果已在运行则跳过）
# 然后单独启动前端
cd D:\baiyuyunma\baiyu-github\baiyu-github\baiyu-web
npm run dev
```

---

## 📊 时间统计

| 启动类型 | 预计耗时 | 适用场景 |
|---------|---------|---------|
| 完整启动（含编译） | ~120 秒 | 首次启动、代码修改后 |
| 快速启动（跳过编译） | ~80 秒 | 日常重启、无代码变化 |
| 仅后端（跳过编译） | ~60 秒 | 后端接口调试 |
| 仅前端（跳过编译） | ~20 秒 | 前端页面调试 |

---

## 🎯 最佳实践

### 1. 添加到 PowerShell 配置文件

编辑 `Documents\WindowsPowerShell\profile.ps1`，添加：

```powershell
function Start-RuoYi {
    Set-Location D:\baiyuyunma\baiyu-github\baiyu-github
    .\scripts\start-project.ps1 @args
}
```

**之后可以这样启动**：
```powershell
Start-RuoYi              # 完整启动
Start-RuoYi -SkipCompile # 跳过编译
```

---

### 2. 创建桌面快捷方式

**目标**：
```
powershell.exe -NoExit -Command "cd D:\baiyuyunma\baiyu-github\baiyu-github; .\scripts\start-project.ps1"
```

**说明**：双击即可启动项目，无需手动输入命令。

---

### 3. 配合 IDE 使用

**VSCode 集成终端**：
1. 打开 VSCode 终端
2. 切换到项目根目录
3. 运行 `.\scripts\start-project.ps1`
4. 在 IDE 中直接查看日志

**IDEA 集成**：
1. Run → Edit Configurations
2. 添加 Shell Script 配置
3. 脚本路径指向 `start-project.ps1`
4. 一键启动/停止

---

## 🆘 常见问题

### Q: 为什么要打开新窗口？

**A**: 
- 后台运行，不阻塞当前终端
- 可以同时打开多个服务窗口
- 方便独立查看前后端日志
- 关闭脚本窗口不影响当前工作

---

### Q: 如何停止服务？

**A**: 
- **方法 1**：关闭启动的 PowerShell 窗口
- **方法 2**：在当前终端执行 `taskkill /f /im java.exe` 和 `taskkill /f /im node.exe`
- **方法 3**：重新运行启动脚本（会自动停止旧进程）

---

### Q: 能否在一个窗口启动？

**A**: 可以，但不推荐。如果确实需要：

```powershell
# 前台启动后端（会阻塞当前终端）
cd D:\baiyuyunma\baiyu-github\baiyu-github\baiyu-ruoyi\ruoyi-admin-wms
mvn spring-boot:run

# 另开终端启动前端
cd ..\..\baiyu-web
npm run dev
```

---

## 📝 更新日志

### v1.0 (2026-03-28)
- ✅ 实现全自动化启动流程
- ✅ 5 阶段进度显示
- ✅ 智能错误处理
- ✅ 后台并行启动
- ✅ 端口连通性检测
- ✅ 编译时间统计
- ✅ 支持多个可选参数

---

**最后更新**: 2026-03-28  
**维护者**: MM (project-ops)  
**反馈**: 通过 MM 主技能联系
