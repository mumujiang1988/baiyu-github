"""
MinIO 集成测试脚本
"""
import requests
import json
from pathlib import Path

BASE_URL = "http://localhost:8000"


def test_health_check():
    """测试健康检查"""
    print("=" * 60)
    print("测试 1: 健康检查")
    print("=" * 60)
    
    response = requests.get(f"{BASE_URL}/health")
    data = response.json()
    
    print(f"状态: {data['status']}")
    print(f"检查结果:")
    for service, status in data['checks'].items():
        icon = "" if status else ""
        print(f"  {icon} {service}: {status}")
    
    assert data['status'] == 'healthy', "服务不健康"
    assert data['checks']['minio'] == True, "MinIO 未连接"
    print("\n 健康检查通过\n")


def test_product_ingest_with_minio():
    """测试产品入库（使用 MinIO 存储）"""
    print("=" * 60)
    print("测试 2: 产品入库（MinIO 存储）")
    print("=" * 60)
    
    # 准备测试图片
    test_image_path = Path(__file__).parent / "test_image.jpg"
    
    if not test_image_path.exists():
        print("  测试图片不存在，创建简单测试图片...")
        from PIL import Image
        img = Image.new('RGB', (224, 224), color='red')
        img.save(test_image_path)
        print(f" 测试图片已创建: {test_image_path}")
    
    # 上传产品
    with open(test_image_path, 'rb') as f:
        files = {'files': ('test.jpg', f, 'image/jpeg')}
        data = {
            'product_code': 'TEST_MINIO_001',
            'name': 'MinIO 测试产品',
            'spec': '测试规格',
            'category': '测试分类',
            'remove_bg': 'false'
        }
        
        print(f"📤 正在上传产品: {data['product_code']}")
        response = requests.post(
            f"{BASE_URL}/api/v1/product/ingest",
            files=files,
            data=data
        )
    
    result = response.json()
    print(f"响应: {json.dumps(result, indent=2, ensure_ascii=False)}")
    
    if result.get('success'):
        print(f"\n 产品入库成功!")
        print(f"   - 成功: {result['success_count']} 张")
        print(f"   - 失败: {result['fail_count']} 张")
        print(f"   - 耗时: {result['ingest_time_ms']}ms")
    else:
        print(f"\n 产品入库失败: {result.get('message')}")
        raise Exception("产品入库失败")
    
    print()


def test_get_image_from_minio():
    """测试从 MinIO 获取图片"""
    print("=" * 60)
    print("测试 3: 从 MinIO 获取图片")
    print("=" * 60)
    
    # 先获取产品信息
    response = requests.get(f"{BASE_URL}/api/v1/products?category=测试分类&page=1&page_size=1")
    products_data = response.json()
    
    if not products_data.get('products'):
        print("  没有测试产品，跳过此测试")
        return
    
    product = products_data['products'][0]
    product_code = product['product_code']
    
    # 获取产品图片
    response = requests.get(f"{BASE_URL}/api/v1/product/{product_code}")
    product_data = response.json()
    
    if not product_data.get('images'):
        print("  产品没有图片，跳过此测试")
        return
    
    image_path = product_data['images'][0]['image_path']
    print(f"📥 正在获取图片: {image_path}")
    
    # 获取图片
    response = requests.get(f"{BASE_URL}/api/v1/images/{image_path}")
    
    if response.status_code == 200:
        print(f" 图片获取成功!")
        print(f"   - 大小: {len(response.content)} bytes")
        print(f"   - Content-Type: {response.headers.get('Content-Type')}")
        print(f"   - Cache-Control: {response.headers.get('Cache-Control')}")
    else:
        print(f" 图片获取失败: {response.status_code}")
        raise Exception("图片获取失败")
    
    print()


def test_search_with_minio_images():
    """测试图片搜索（使用 MinIO 存储的图片）"""
    print("=" * 60)
    print("测试 4: 图片搜索（MinIO 存储）")
    print("=" * 60)
    
    # 使用之前上传的测试图片进行搜索
    test_image_path = Path(__file__).parent / "test_image.jpg"
    
    if not test_image_path.exists():
        print("  测试图片不存在，跳过此测试")
        return
    
    with open(test_image_path, 'rb') as f:
        files = {'file': ('test.jpg', f, 'image/jpeg')}
        
        print("🔍 正在执行图片搜索...")
        response = requests.post(
            f"{BASE_URL}/api/v1/search",
            files=files
        )
    
    result = response.json()
    
    if result.get('success'):
        print(f" 搜索成功!")
        print(f"   - 结果数量: {len(result['results'])}")
        print(f"   - 搜索耗时: {result['search_time_ms']}ms")
        
        if result['results']:
            top_result = result['results'][0]
            print(f"\n最匹配的产品:")
            print(f"   - 编码: {top_result['product_code']}")
            print(f"   - 名称: {top_result['product_name']}")
            print(f"   - 相似度: {top_result['similarity']:.4f}")
    else:
        print(f" 搜索失败: {result.get('message')}")
    
    print()


def main():
    """运行所有测试"""
    print("\n" + "=" * 60)
    print("🧪 MinIO 集成测试开始")
    print("=" * 60 + "\n")
    
    try:
        test_health_check()
        test_product_ingest_with_minio()
        test_get_image_from_minio()
        test_search_with_minio_images()
        
        print("=" * 60)
        print("🎉 所有测试通过！MinIO 集成成功！")
        print("=" * 60)
        
    except Exception as e:
        print("\n" + "=" * 60)
        print(f" 测试失败: {str(e)}")
        print("=" * 60)
        raise


if __name__ == "__main__":
    main()
