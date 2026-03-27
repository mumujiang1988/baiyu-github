# -*- coding: utf-8 -*-
"""
更新字典配置中的 API 路径
将旧的 /erp/engine/dictionary/{name}/data?moduleCode={moduleCode}
改为新的 /erp/engine/dict/union/{name}
"""

import json
import pymysql

# 数据库连接配置
db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': 'hanzhiyun1988',
    'database': 'test',
    'charset': 'utf8mb4'
}

try:
    # 连接数据库
    connection = pymysql.connect(**db_config)
    cursor = connection.cursor()
    
    print("✓ 数据库连接成功\n")
    
    # 查询当前配置
    select_sql = """
    SELECT config_id, module_code, dict_config 
    FROM erp_page_config 
    WHERE module_code = 'saleorder'
    """
    
    cursor.execute(select_sql)
    result = cursor.fetchone()
    
    if result:
        config_id, module_code, dict_config_json = result
        
        print(f"找到配置:")
        print(f"  Config ID: {config_id}")
        print(f"  Module Code: {module_code}\n")
        
        # 解析 JSON
        dict_config = json.loads(dict_config_json)
        
        # 更新 dictionaries 中的 API 路径
        dictionaries = dict_config.get('dictionaries', {})
        updated_count = 0
        
        for dict_name, dict_value in dictionaries.items():
            if dict_value.get('type') == 'dynamic' and dict_value.get('config'):
                old_api = dict_value['config'].get('api', '')
                
                if 'moduleCode' in old_api:
                    # 替换为新 API 路径
                    new_api = f"/erp/engine/dict/union/{dict_name}"
                    dict_value['config']['api'] = new_api
                    print(f"  ✓ 更新 {dict_name}: {old_api}")
                    print(f"    → {new_api}")
                    updated_count += 1
                elif 'union' not in old_api:
                    print(f"  ✓ {dict_name} 已经是新格式：{old_api}")
        
        # 更新数据库
        if updated_count > 0:
            update_sql = """
            UPDATE erp_page_config 
            SET dict_config = %s, version = version + 1, update_time = NOW(), update_by = 'system'
            WHERE config_id = %s
            """
            
            new_json = json.dumps(dict_config, ensure_ascii=False)
            cursor.execute(update_sql, (new_json, config_id))
            connection.commit()
            
            print(f"\n✅ 更新成功!")
            print(f"  更新了 {updated_count} 个字典的 API 路径")
            print(f"  影响行数：{cursor.rowcount}")
            
            # 验证更新结果
            verify_sql = """
            SELECT 
              JSON_EXTRACT(dict_config, '$.builder.enabled') as builder_enabled,
              JSON_LENGTH(JSON_EXTRACT(dict_config, '$.dictionaries')) as dict_count
            FROM erp_page_config
            WHERE config_id = %s
            """
            cursor.execute(verify_sql, (config_id,))
            verify_result = cursor.fetchone()
            
            print(f"\n验证结果:")
            print(f"  Builder Enabled: {verify_result[0]}")
            print(f"  Dictionary Count: {verify_result[1]}")
        else:
            print(f"\n无需更新，所有 API 路径已经正确")
    else:
        print("❌ 未找到销售订单模块的配置")
        
except Exception as e:
    print(f"\n错误：{e}")
    
finally:
    if 'connection' in locals():
        connection.close()
        print("\n数据库连接已关闭")
