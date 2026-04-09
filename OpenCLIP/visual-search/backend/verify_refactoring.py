"""
代码拆分验证脚本

验证拆分后的代码结构和功能完整性
"""
import os
import sys


def check_file_structure():
    """检查文件结构"""
    print("=" * 60)
    print("📁 文件结构验证")
    print("=" * 60)
    
    required_files = [
        "main.py",
        "config/__init__.py",
        "config/settings.py",
        "dependencies/__init__.py",
        "routers/__init__.py",
        "routers/search.py",
        "routers/product.py",
        "routers/image.py"
    ]
    
    missing = []
    for file in required_files:
        path = os.path.join(os.path.dirname(__file__), file)
        if os.path.exists(path):
            lines = len(open(path, 'r', encoding='utf-8').readlines())
            status = "✅" if lines < 500 else "⚠️"
            print(f"{status} {file}: {lines} 行")
        else:
            print(f"❌ {file}: 缺失")
            missing.append(file)
    
    if missing:
        print(f"\n❌ 缺少 {len(missing)} 个文件")
        return False
    
    print("\n✅ 所有必需文件存在")
    return True


def check_main_py_size():
    """检查 main.py 大小"""
    print("\n" + "=" * 60)
    print("📊 main.py 大小验证")
    print("=" * 60)
    
    main_path = os.path.join(os.path.dirname(__file__), "main.py")
    lines = len(open(main_path, 'r', encoding='utf-8').readlines())
    
    print(f"main.py 行数: {lines}")
    
    if lines < 200:
        print(f"✅ 主文件已精简至 {lines} 行（原963行，减少 {100 - (lines/963)*100:.1f}%）")
        return True
    else:
        print(f"⚠️ 主文件仍较大 ({lines} 行)")
        return False


def check_imports():
    """检查导入语句"""
    print("\n" + "=" * 60)
    print("🔗 导入语句验证")
    print("=" * 60)
    
    main_path = os.path.join(os.path.dirname(__file__), "main.py")
    content = open(main_path, 'r', encoding='utf-8').read()
    
    required_imports = [
        "from config import settings",
        "from dependencies import init_services",
        "from routers import search_router, product_router, image_router"
    ]
    
    all_ok = True
    for imp in required_imports:
        if imp in content:
            print(f"✅ {imp}")
        else:
            print(f"❌ 缺少: {imp}")
            all_ok = False
    
    return all_ok


def check_routers():
    """检查路由器注册"""
    print("\n" + "=" * 60)
    print("🛣️  路由器注册验证")
    print("=" * 60)
    
    main_path = os.path.join(os.path.dirname(__file__), "main.py")
    content = open(main_path, 'r', encoding='utf-8').read()
    
    routers = ["search_router", "product_router", "image_router"]
    
    all_ok = True
    for router in routers:
        if f"include_router({router})" in content:
            print(f"✅ {router} 已注册")
        else:
            print(f"❌ {router} 未注册")
            all_ok = False
    
    return all_ok


def check_api_endpoints():
    """检查API端点定义"""
    print("\n" + "=" * 60)
    print("🔌 API端点验证")
    print("=" * 60)
    
    expected_endpoints = {
        "routers/search.py": [
            '@router.post("/search"',  # 匹配 @router.post("/search", response_model=...)
            '@router.post("/search/text")',
            '@router.post("/search/hybrid")'
        ],
        "routers/product.py": [
            "@router.post(\"/product/ingest\")",
            "@router.post(\"/products/batch-ingest\")",
            "@router.get(\"/product/{product_code}\")",
            "@router.delete(\"/product/{product_code}\")",
            "@router.delete(\"/products/batch\")"
        ],
        "routers/image.py": [
            "@router.get(\"/images/{image_path:path}\")",
            "@router.post(\"/rembg/remove\")"
        ]
    }
    
    all_ok = True
    for file, endpoints in expected_endpoints.items():
        filepath = os.path.join(os.path.dirname(__file__), file)
        if not os.path.exists(filepath):
            print(f"❌ {file}: 文件不存在")
            all_ok = False
            continue
        
        content = open(filepath, 'r', encoding='utf-8').read()
        
        print(f"\n{file}:")
        for endpoint in endpoints:
            if endpoint in content:
                print(f"  ✅ {endpoint}")
            else:
                print(f"  ❌ 缺少: {endpoint}")
                all_ok = False
    
    return all_ok


def check_line_limits():
    """检查文件行数限制"""
    print("\n" + "=" * 60)
    print("📏 文件行数限制验证 (<500行)")
    print("=" * 60)
    
    files_to_check = [
        "main.py",
        "config/settings.py",
        "dependencies/__init__.py",
        "routers/search.py",
        "routers/product.py",
        "routers/image.py"
    ]
    
    violations = []
    
    for file in files_to_check:
        filepath = os.path.join(os.path.dirname(__file__), file)
        if os.path.exists(filepath):
            lines = len(open(filepath, 'r', encoding='utf-8').readlines())
            status = "✅" if lines < 500 else "❌"
            print(f"{status} {file}: {lines} 行")
            
            if lines >= 500:
                violations.append((file, lines))
    
    if violations:
        print(f"\n⚠️  {len(violations)} 个文件超过500行:")
        for file, lines in violations:
            print(f"   - {file}: {lines} 行")
        return False
    
    print("\n✅ 所有文件均小于500行")
    return True


def main():
    """主函数"""
    print("\n🔍 开始代码拆分验证...\n")
    
    results = {
        "文件结构": check_file_structure(),
        "main.py大小": check_main_py_size(),
        "导入语句": check_imports(),
        "路由器注册": check_routers(),
        "API端点": check_api_endpoints(),
        "行数限制": check_line_limits()
    }
    
    print("\n" + "=" * 60)
    print("📊 验证总结")
    print("=" * 60)
    
    for check, result in results.items():
        status = "✅ 通过" if result else "❌ 失败"
        print(f"{check}: {status}")
    
    all_passed = all(results.values())
    
    print("\n" + "=" * 60)
    if all_passed:
        print("🎉 所有验证通过！代码拆分成功！")
        print("=" * 60)
        return 0
    else:
        print("⚠️  部分验证失败，请检查上述问题")
        print("=" * 60)
        return 1


if __name__ == "__main__":
    sys.exit(main())
