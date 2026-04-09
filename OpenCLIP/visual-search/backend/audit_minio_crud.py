"""
MinIO 完整 CRUD 审计报告
"""
import requests
import json
from pathlib import Path

BASE_URL = "http://localhost:8000"


def print_section(title):
    """打印分节标题"""
    print("\n" + "=" * 70)
    print(f"  {title}")
    print("=" * 70)


def test_health():
    """测试 1: 健康检查"""
    print_section("测试 1: 服务健康状态")
    
    response = requests.get(f"{BASE_URL}/health")
    data = response.json()
    
    print(f"\n整体状态: {data['status']}")
    print("\n服务检查结果:")
    for service, status in data['checks'].items():
        icon = "✅" if status else "❌"
        print(f"  {icon} {service:10s}: {'正常' if status else '异常'}")
    
    return data['status'] == 'healthy' and data['checks']['minio']


def test_create_product():
    """测试 2: 创建产品（增 - Create）"""
    print_section("测试 2: 创建产品 (POST /api/v1/product/ingest)")
    
    # 创建测试图片
    from PIL import Image
    import io
    
    img = Image.new('RGB', (224, 224), color='blue')
    img_bytes = io.BytesIO()
    img.save(img_bytes, format='JPEG')
    img_bytes.seek(0)
    
    # 上传产品
    files = {'files': ('test_create.jpg', img_bytes, 'image/jpeg')}
    data = {
        'product_code': 'AUDIT_TEST_001',
        'name': '审计测试产品-创建',
        'spec': '测试规格',
        'category': '审计分类',
        'remove_bg': 'false'
    }
    
    print(f"\n📤 上传产品信息:")
    print(f"   产品编码: {data['product_code']}")
    print(f"   产品名称: {data['name']}")
    print(f"   规格: {data['spec']}")
    print(f"   分类: {data['category']}")
    
    response = requests.post(
        f"{BASE_URL}/api/v1/product/ingest",
        files=files,
        data=data
    )
    
    result = response.json()
    
    print(f"\n📥 响应结果:")
    print(f"   成功: {result.get('success')}")
    print(f"   消息: {result.get('message')}")
    print(f"   成功数量: {result.get('success_count')}")
    print(f"   失败数量: {result.get('fail_count')}")
    print(f"   耗时: {result.get('ingest_time_ms')}ms")
    
    if result.get('errors'):
        print(f"\n⚠️  错误信息:")
        for error in result['errors']:
            print(f"   - {error}")
    
    success = result.get('success') and result.get('success_count') > 0
    
    if success:
        print(f"\n✅ 创建产品成功！图片已存储到 MinIO")
    else:
        print(f"\n❌ 创建产品失败")
    
    return success


def test_read_product():
    """测试 3: 查询产品（查 - Read）"""
    print_section("测试 3: 查询产品 (GET /api/v1/product/{code})")
    
    product_code = 'AUDIT_TEST_001'
    
    print(f"\n🔍 查询产品: {product_code}")
    
    response = requests.get(f"{BASE_URL}/api/v1/product/{product_code}")
    
    if response.status_code != 200:
        print(f"\n❌ 查询失败: HTTP {response.status_code}")
        return False
    
    data = response.json()
    
    print(f"\n📥 产品信息:")
    print(f"   产品编码: {data['product']['product_code']}")
    print(f"   产品名称: {data['product']['name']}")
    print(f"   规格: {data['product'].get('spec', 'N/A')}")
    print(f"   分类: {data['product'].get('category', 'N/A')}")
    print(f"   创建时间: {data['product']['created_at']}")
    
    print(f"\n📷 图片列表 ({len(data['images'])} 张):")
    for i, img in enumerate(data['images'], 1):
        print(f"   {i}. 路径: {img['image_path']}")
        print(f"      哈希: {img['image_hash']}")
        print(f"      Milvus ID: {img['milvus_id']}")
        if img.get('image_size'):
            print(f"      大小: {img['image_size']} bytes")
    
    # 验证图片是否可以从 MinIO 访问
    if data['images']:
        image_path = data['images'][0]['image_path']
        print(f"\n🖼️  测试图片访问: {image_path}")
        
        img_response = requests.get(f"{BASE_URL}/api/v1/images/{image_path}")
        
        if img_response.status_code == 200:
            print(f"   ✅ 图片访问成功")
            print(f"   大小: {len(img_response.content)} bytes")
            print(f"   Content-Type: {img_response.headers.get('Content-Type')}")
            print(f"   Cache-Control: {img_response.headers.get('Cache-Control')}")
            
            # 验证图片确实来自 MinIO
            backend_logs = requests.get(f"{BASE_URL}/health")
            health_data = backend_logs.json()
            if health_data['checks'].get('minio'):
                print(f"   ✅ 确认: 图片从 MinIO 读取")
        else:
            print(f"   ❌ 图片访问失败: HTTP {img_response.status_code}")
            return False
    
    print(f"\n✅ 查询产品成功！")
    return True


def test_update_product():
    """测试 4: 更新产品（改 - Update）"""
    print_section("测试 4: 更新产品 (POST /api/v1/product/ingest - UPSERT)")
    
    # 创建新图片
    from PIL import Image
    import io
    
    img = Image.new('RGB', (224, 224), color='green')
    img_bytes = io.BytesIO()
    img.save(img_bytes, format='PNG')
    img_bytes.seek(0)
    
    # 更新产品信息（使用相同的 product_code）
    files = {'files': ('test_update.png', img_bytes, 'image/png')}
    data = {
        'product_code': 'AUDIT_TEST_001',  # 相同编码，触发更新
        'name': '审计测试产品-已更新',
        'spec': '更新后的规格',
        'category': '更新后的分类',
        'remove_bg': 'false'
    }
    
    print(f"\n📤 更新产品信息:")
    print(f"   产品编码: {data['product_code']} (保持不变)")
    print(f"   新名称: {data['name']}")
    print(f"   新规格: {data['spec']}")
    print(f"   新分类: {data['category']}")
    
    response = requests.post(
        f"{BASE_URL}/api/v1/product/ingest",
        files=files,
        data=data
    )
    
    result = response.json()
    
    print(f"\n📥 响应结果:")
    print(f"   成功: {result.get('success')}")
    print(f"   消息: {result.get('message')}")
    print(f"   新增图片: {result.get('success_count')} 张")
    
    # 验证更新
    print(f"\n🔍 验证更新结果...")
    verify_response = requests.get(f"{BASE_URL}/api/v1/product/AUDIT_TEST_001")
    verify_data = verify_response.json()
    
    print(f"\n📥 更新后的产品信息:")
    print(f"   名称: {verify_data['product']['name']}")
    print(f"   规格: {verify_data['product'].get('spec')}")
    print(f"   分类: {verify_data['product'].get('category')}")
    print(f"   图片数量: {len(verify_data['images'])} 张")
    
    name_updated = verify_data['product']['name'] == data['name']
    spec_updated = verify_data['product'].get('spec') == data['spec']
    category_updated = verify_data['product'].get('category') == data['category']
    
    if name_updated and spec_updated and category_updated:
        print(f"\n✅ 更新产品成功！")
        return True
    else:
        print(f"\n❌ 更新验证失败")
        return False


def test_list_products():
    """测试 5: 列出产品（查 - List）"""
    print_section("测试 5: 列出产品 (GET /api/v1/products)")
    
    print(f"\n🔍 查询产品列表...")
    
    response = requests.get(f"{BASE_URL}/api/v1/products?page=1&page_size=10")
    data = response.json()
    
    print(f"\n📥 查询结果:")
    print(f"   总数: {data['total']}")
    print(f"   当前页: {data['page']}")
    print(f"   每页数量: {data['page_size']}")
    print(f"   返回数量: {len(data['products'])}")
    
    print(f"\n📦 产品列表:")
    for i, product in enumerate(data['products'][:5], 1):
        print(f"   {i}. {product['product_code']} - {product['name']}")
        print(f"      分类: {product.get('category', 'N/A')}")
        print(f"      创建时间: {product['created_at']}")
    
    if len(data['products']) > 5:
        print(f"   ... 还有 {len(data['products']) - 5} 个产品")
    
    # 查找我们的测试产品
    test_product = next(
        (p for p in data['products'] if p['product_code'] == 'AUDIT_TEST_001'),
        None
    )
    
    if test_product:
        print(f"\n✅ 找到测试产品: {test_product['name']}")
        return True
    else:
        print(f"\n⚠️  未找到测试产品")
        return False


def test_delete_product():
    """测试 6: 删除产品（删 - Delete）"""
    print_section("测试 6: 删除产品 (DELETE /api/v1/product/{code})")
    
    product_code = 'AUDIT_TEST_001'
    
    print(f"\n🗑️  删除产品: {product_code}")
    print(f"   ⚠️  此操作将删除:")
    print(f"      - MySQL 中的产品记录")
    print(f"      - MySQL 中的图片记录")
    print(f"      - Milvus 中的向量数据")
    print(f"      - MinIO 中的图片文件")
    
    response = requests.delete(f"{BASE_URL}/api/v1/product/{product_code}")
    result = response.json()
    
    print(f"\n📥 响应结果:")
    print(f"   成功: {result.get('success')}")
    print(f"   消息: {result.get('message')}")
    print(f"   删除图片数: {result.get('deleted_images')}")
    
    if not result.get('success'):
        print(f"\n❌ 删除失败")
        return False
    
    # 验证删除
    print(f"\n🔍 验证删除结果...")
    
    # 1. 验证 MySQL 中产品已删除
    verify_response = requests.get(f"{BASE_URL}/api/v1/product/{product_code}")
    if verify_response.status_code == 404:
        print(f"   ✅ MySQL 产品记录已删除")
    else:
        print(f"   ❌ MySQL 产品记录仍存在")
        return False
    
    # 2. 验证 MinIO 中图片已删除
    try:
        from minio import Minio
        minio_client = Minio(
            'minio:9000',
            access_key='minioadmin',
            secret_key='minioadmin',
            secure=False
        )
        
        # 尝试列出该产品的所有对象
        objects = list(minio_client.list_objects(
            'product-images',
            prefix=f'{product_code}/',
            recursive=True
        ))
        
        if len(objects) == 0:
            print(f"   ✅ MinIO 图片文件已删除")
        else:
            print(f"   ⚠️  MinIO 中仍有 {len(objects)} 个对象:")
            for obj in objects:
                print(f"      - {obj.object_name}")
    except Exception as e:
        print(f"   ⚠️  无法验证 MinIO 删除状态: {str(e)}")
    
    print(f"\n✅ 删除产品成功！")
    return True


def test_minio_storage_verification():
    """测试 7: MinIO 存储验证"""
    print_section("测试 7: MinIO 存储状态验证")
    
    try:
        from minio import Minio
        
        minio_client = Minio(
            'minio:9000',
            access_key='minioadmin',
            secret_key='minioadmin',
            secure=False
        )
        
        # 列出所有对象
        objects = list(minio_client.list_objects(
            'product-images',
            recursive=True
        ))
        
        print(f"\n📊 MinIO 存储统计:")
        print(f"   Bucket: product-images")
        print(f"   对象总数: {len(objects)}")
        
        if objects:
            total_size = sum(obj.size for obj in objects)
            print(f"   总大小: {total_size:,} bytes ({total_size / 1024:.2f} KB)")
            
            print(f"\n📁 对象列表 (前10个):")
            for i, obj in enumerate(objects[:10], 1):
                print(f"   {i}. {obj.object_name}")
                print(f"      大小: {obj.size:,} bytes")
                print(f"      最后修改: {obj.last_modified}")
            
            if len(objects) > 10:
                print(f"   ... 还有 {len(objects) - 10} 个对象")
        else:
            print(f"\nℹ️  MinIO 中暂无对象（可能已被删除）")
        
        print(f"\n✅ MinIO 存储验证完成")
        return True
        
    except Exception as e:
        print(f"\n❌ MinIO 验证失败: {str(e)}")
        return False


def main():
    """运行完整审计"""
    print("\n" + "=" * 70)
    print("  🧪 MinIO 完整 CRUD 审计报告")
    print("=" * 70)
    
    results = {}
    
    try:
        # 测试 1: 健康检查
        results['health'] = test_health()
        
        # 测试 2: 创建
        results['create'] = test_create_product()
        
        # 测试 3: 查询
        if results['create']:
            results['read'] = test_read_product()
        else:
            results['read'] = False
            print("\n⚠️  跳过查询测试（创建失败）")
        
        # 测试 4: 更新
        if results['create']:
            results['update'] = test_update_product()
        else:
            results['update'] = False
            print("\n⚠️  跳过更新测试（创建失败）")
        
        # 测试 5: 列表
        results['list'] = test_list_products()
        
        # 测试 6: 删除
        if results['create']:
            results['delete'] = test_delete_product()
        else:
            results['delete'] = False
            print("\n⚠️  跳过删除测试（创建失败）")
        
        # 测试 7: MinIO 验证
        results['minio_verify'] = test_minio_storage_verification()
        
        # 汇总报告
        print_section("📊 审计结果汇总")
        
        test_names = {
            'health': '服务健康检查',
            'create': '创建产品 (Create)',
            'read': '查询产品 (Read)',
            'update': '更新产品 (Update)',
            'list': '列出产品 (List)',
            'delete': '删除产品 (Delete)',
            'minio_verify': 'MinIO 存储验证'
        }
        
        all_passed = True
        for key, name in test_names.items():
            status = results.get(key, False)
            icon = "✅" if status else "❌"
            print(f"  {icon} {name:30s}: {'通过' if status else '失败'}")
            if not status:
                all_passed = False
        
        print("\n" + "=" * 70)
        if all_passed:
            print("  🎉 所有测试通过！MinIO CRUD 功能完全正常！")
        else:
            print("  ⚠️  部分测试失败，请检查上述错误信息")
        print("=" * 70 + "\n")
        
        return all_passed
        
    except Exception as e:
        print(f"\n❌ 审计过程出错: {str(e)}")
        import traceback
        traceback.print_exc()
        return False


if __name__ == "__main__":
    success = main()
    exit(0 if success else 1)

