# 更新字典配置中的 API 路径
# 将旧的 /erp/engine/dictionary/{name}/data?moduleCode={moduleCode}
# 改为新的 /erp/engine/dict/union/{name}

$connectionString = "Server=localhost;Database=test;Uid=root;Pwd=hanzhiyun1988;"

# 创建连接
$connection = New-Object MySql.Data.MySqlClient.MySqlConnection
$connection.ConnectionString = $connectionString

try {
    $connection.Open()
    
    # 查询当前配置
    $command = $connection.CreateCommand()
    $command.CommandText = @"
SELECT config_id, module_code, dict_config 
FROM erp_page_config 
WHERE module_code = 'saleorder'
"@
    
    $reader = $command.ExecuteReader()
    
    if ($reader.Read()) {
        $configId = $reader.GetInt32("config_id")
        $moduleCode = $reader.GetString("module_code")
        $dictConfigJson = $reader.GetString("dict_config")
        
        Write-Host "找到配置:" -ForegroundColor Green
        Write-Host "  Config ID: $configId"
        Write-Host "  Module Code: $moduleCode"
        
        # 解析 JSON
        $dictConfig = [Newtonsoft.Json.Linq.JObject]::Parse($dictConfigJson)
        
        # 更新 dictionaries 中的 API 路径
        $dictionaries = $dictConfig["dictionaries"]
        $updatedCount = 0
        
        foreach ($dict in $dictionaries) {
            $dictName = ($dict -as [System.Collections.DictionaryEntry]).Key
            $dictValue = ($dict -as [System.Collections.DictionaryEntry]).Value
            
            if ($dictValue["type"] -eq "dynamic" -and $dictValue["config"] -ne $null) {
                $apiNode = $dictValue["config"]["api"]
                $oldApi = if ($apiNode -ne $null) { $apiNode.ToString() } else { $null }
                
                if ($oldApi -like "*moduleCode*") {
                    # 替换为新 API 路径
                    $newApi = "/erp/engine/dict/union/$dictName"
                    $dictValue["config"]["api"] = $newApi
                    Write-Host "  ✓ 更新 $dictName : $oldApi → $newApi" -ForegroundColor Yellow
                    $updatedCount++
                } elseif ($oldApi -notlike "*union*") {
                    # 已经是新格式，跳过
                    Write-Host "  ✓ $dictName 已经是新格式：$oldApi" -ForegroundColor Green
                }
            }
        }
        
        $reader.Close()
        
        if ($updatedCount -gt 0) {
            # 更新数据库
            $updateCommand = $connection.CreateCommand()
            $updateCommand.CommandText = @"
UPDATE erp_page_config 
SET dict_config = @dictConfig, version = version + 1, update_time = NOW()
WHERE config_id = @configId
"@
            $updateCommand.Parameters.AddWithValue("@dictConfig", $dictConfig.ToString()) | Out-Null
            $updateCommand.Parameters.AddWithValue("@configId", $configId) | Out-Null
            
            $rowsAffected = $updateCommand.ExecuteNonQuery()
            
            Write-Host "`n更新成功!" -ForegroundColor Green
            Write-Host "  更新了 $updatedCount 个字典的 API 路径"
            Write-Host "  影响行数：$rowsAffected"
            
            # 验证更新结果
            $verifyCommand = $connection.CreateCommand()
            $verifyCommand.CommandText = @"
SELECT 
  JSON_EXTRACT(dict_config, '$.builder.enabled') as builder_enabled,
  JSON_LENGTH(JSON_EXTRACT(dict_config, '$.dictionaries')) as dict_count
FROM erp_page_config
WHERE config_id = @configId
"@
            $verifyCommand.Parameters.AddWithValue("@configId", $configId) | Out-Null
            
            $result = $verifyCommand.ExecuteScalar()
            Write-Host "`n验证结果:" -ForegroundColor Green
            Write-Host "  Builder Enabled: $result"
        } else {
            Write-Host "`n无需更新，所有 API 路径已经正确" -ForegroundColor Cyan
        }
    } else {
        Write-Host "未找到销售订单模块的配置" -ForegroundColor Red
    }
} catch {
    Write-Host "错误：$($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.InnerException) {
        Write-Host "详细：$($_.Exception.InnerException.Message)" -ForegroundColor Red
    }
} finally {
    if ($connection.State -eq 'Open') {
        $connection.Close()
    }
}
