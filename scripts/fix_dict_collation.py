# -*- coding: utf-8 -*-
"""
修复字典表字符集冲突问题
解决 UNION 操作时的 Illegal mix of collations 错误
"""

import pymysql

def fix_collation():
    """修改 bymaterial_dictionary 表的字符集"""
    try:
        # 连接数据库
        conn = pymysql.connect(
            host='localhost',
            user='root',
            password='hanzhiyun1988',
            database='test',
            charset='utf8mb4'
        )
        
        cursor = conn.cursor()
        
        print("🔍 正在修改 bymaterial_dictionary 表的字符集...")
        
        # 修改 name 和 kingdee 字段的字符集为 utf8mb4_general_ci
        sql = """
        ALTER TABLE bymaterial_dictionary 
        MODIFY COLUMN name VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '名称',
        MODIFY COLUMN kingdee VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '金蝶代码'
        """
        
        cursor.execute(sql)
        conn.commit()
        
        print("✅ 字符集修改成功！")
        
        # 验证修改结果
        print("\n📊 当前字符集设置:")
        cursor.execute("""
        SELECT COLUMN_NAME, CHARACTER_SET_NAME, COLLATION_NAME
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = 'test'
          AND TABLE_NAME = 'bymaterial_dictionary'
          AND COLUMN_NAME IN ('name', 'kingdee')
        """)
        
        rows = cursor.fetchall()
        for row in rows:
            print(f"  {row[0]}: {row[1]}/{row[2]}")
        
        # 检查 sys_dict_data 表的字符集
        print("\n📊 sys_dict_data 表字符集:")
        cursor.execute("""
        SELECT COLUMN_NAME, CHARACTER_SET_NAME, COLLATION_NAME
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = 'test'
          AND TABLE_NAME = 'sys_dict_data'
          AND COLUMN_NAME IN ('dict_label', 'dict_value')
        """)
        
        rows = cursor.fetchall()
        for row in rows:
            print(f"  {row[0]}: {row[1]}/{row[2]}")
        
        cursor.close()
        conn.close()
        
        print("\n✅ 字符集冲突问题已修复！请刷新浏览器测试。")
        
    except Exception as e:
        print(f"❌ 执行失败：{e}")
        import traceback
        traceback.print_exc()

if __name__ == '__main__':
    fix_collation()
