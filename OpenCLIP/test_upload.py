#!/usr/bin/env python3
"""
测试图片上传功能
"""
import requests
from PIL import Image
import io
import numpy as np

# 创建一个测试图片
def create_test_image():
    """创建一个简单的测试图片"""
    # 创建一个随机图片
    img_array = np.random.randint(0, 255, (224, 224, 3), dtype=np.uint8)
    img = Image.fromarray(img_array)

    # 保存到字节流
    img_bytes = io.BytesIO()
    img.save(img_bytes, format='PNG')
    img_bytes.seek(0)

    return img_bytes

# 测试产品入库
def test_product_ingest():
    """测试产品入库接口"""
    url = "http://localhost:8000/api/v1/product/ingest"

    # 准备数据
    files = [
        ('files', ('file1.png', create_test_image(), 'image/png')),
        ('files', ('file2.png', create_test_image(), 'image/png'))
    ]
    data = {
        'product_code': 'TEST001',
        'name': '测试产品',
        'spec': '测试规格',
        'category': '测试分类'
    }

    try:
        print("正在测试产品入库...")
        response = requests.post(url, files=files, data=data, timeout=60)

        print(f"状态码: {response.status_code}")
        print(f"响应: {response.json()}")

        if response.status_code == 200:
            result = response.json()
            if result.get('success'):
                print("✅ 产品入库成功!")
                print(f"产品编码: {result.get('product_code')}")
                print(f"成功数量: {result.get('success_count')}")
                print(f"失败数量: {result.get('fail_count')}")
                if result.get('errors'):
                    print(f"错误信息: {result.get('errors')}")
                return True
            else:
                print(f"❌ 产品入库失败: {result.get('message')}")
                return False
        else:
            print(f"❌ 请求失败: {response.text}")
            return False

    except Exception as e:
        print(f"❌ 发生异常: {str(e)}")
        return False

# 测试获取产品详情
def test_get_product():
    """测试获取产品详情"""
    url = "http://localhost:8000/api/v1/product/TEST001"

    try:
        print("\n正在测试获取产品详情...")
        response = requests.get(url, timeout=10)

        print(f"状态码: {response.status_code}")

        if response.status_code == 200:
            result = response.json()
            if result.get('success'):
                print("✅ 获取产品详情成功!")
                product = result.get('product')
                images = result.get('images')
                print(f"产品编码: {product.get('product_code')}")
                print(f"产品名称: {product.get('name')}")
                print(f"图片数量: {len(images)}")
                for img in images:
                    print(f"  - {img.get('image_path')} (状态: {img.get('status')})")
                return True
            else:
                print(f"❌ 获取产品详情失败: {result.get('message')}")
                return False
        else:
            print(f"❌ 请求失败: {response.text}")
            return False

    except Exception as e:
        print(f"❌ 发生异常: {str(e)}")
        return False

# 测试图片访问
def test_image_access():
    """测试图片访问接口"""
    image_path = "TEST001/xxxxx.png"  # 这个路径需要根据实际情况调整

    try:
        print(f"\n正在测试图片访问: {image_path}")
        url = f"http://localhost:8000/api/v1/images/{image_path}"
        response = requests.get(url, timeout=10)

        print(f"状态码: {response.status_code}")

        if response.status_code == 200:
            print("✅ 图片访问成功!")
            print(f"内容类型: {response.headers.get('content-type')}")
            print(f"内容长度: {len(response.content)} bytes")
            return True
        else:
            print(f"❌ 图片访问失败: {response.text}")
            return False

    except Exception as e:
        print(f"❌ 发生异常: {str(e)}")
        return False

if __name__ == "__main__":
    print("=" * 50)
    print("图片上传功能测试")
    print("=" * 50)

    # 运行测试
    success = True
    success &= test_product_ingest()
    success &= test_get_product()
    # success &= test_image_access()  # 需要知道实际的图片路径

    print("\n" + "=" * 50)
    if success:
        print("✅ 所有测试通过!")
    else:
        print("❌ 部分测试失败!")
    print("=" * 50)
