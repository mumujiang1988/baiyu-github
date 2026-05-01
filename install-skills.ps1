# 批量安装 Skills 脚本
# 使用方法: 在 PowerShell 中运行 .\install-skills.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "开始安装 Skills..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$skills = @(
    # Official Core Skills
    @{name="vercel-labs/skills"; desc="Vercal Labs Skills Collection"},
    @{name="anthropic/skillcreator"; desc="Custom Skill Creator"},
    @{name="anthropic/findskills"; desc="Skill Search and Discovery"},
    @{name="anthropic/feature-dev"; desc="Requirement to Code Development"},
    
    # Code Core Tools
    @{name="file-search"; desc="Global File Search"},
    @{name="code-format"; desc="Code Formatting (Prettier)"},
    @{name="lsp"; desc="Syntax Checking and Error Validation"},
    
    # Rules Enforcement
    @{name="claude-md-enforce"; desc="Enforce CLAUDE.md Rules"},
    
    # Git & Documentation
    @{name="git-context"; desc="Git Code Diff Awareness"},
    @{name="markdown-renderer"; desc="Markdown Architecture Document Parser"},
    
    # Database & Debugging
    @{name="sql-helper"; desc="SQL Optimization and Field Validation"},
    @{name="debug-helper"; desc="Error Log Analysis and Bug Location"}
)

$total = $skills.Count
$current = 0

foreach ($skill in $skills) {
    $current++
    Write-Host "[$current/$total] 正在安装: $($skill.name)" -ForegroundColor Yellow
    Write-Host "  描述: $($skill.desc)" -ForegroundColor Gray
    
    try {
        npx skills-lc-cli add $skill.name -y
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✓ 安装成功" -ForegroundColor Green
        } else {
            Write-Host "  ✗ 安装失败 (退出码: $LASTEXITCODE)" -ForegroundColor Red
        }
    } catch {
        Write-Host "  ✗ 安装异常: $_" -ForegroundColor Red
    }
    
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "所有技能安装完成!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "提示: 如果某些技能安装失败,可以手动重试:" -ForegroundColor Yellow
Write-Host "npx skills-lc-cli add <skill-name>" -ForegroundColor Gray
