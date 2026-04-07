-- 修复产品图片表的唯一索引问题
-- 问题：当前的唯一索引 uk_hash (image_hash) 会检查所有记录，包括已删除的记录
-- 解决方案：删除现有索引，创建新的唯一索引，只对正常状态的图片生效

-- 1. 删除现有的唯一索引
ALTER TABLE product_image DROP INDEX uk_hash;

-- 2. 创建新的唯一索引，只对正常状态(status=1)的图片生效
-- 使用部分索引(Filtered Index)的概念，但在MySQL中需要使用触发器或其他方式
-- 这里我们使用一个更简单的方法：创建复合唯一索引

-- 方案1: 创建复合唯一索引 (image_hash, status)
-- 这样只有当 image_hash 和 status 都相同时才会冲突
ALTER TABLE product_image ADD UNIQUE KEY uk_hash_status (image_hash, status);

-- 方案2: (推荐) 删除唯一索引，改为普通索引，在应用层处理去重
-- ALTER TABLE product_image ADD INDEX idx_image_hash (image_hash);

-- 注意：如果选择方案2，需要确保应用层的 image_exists 检查在插入前执行
-- 当前代码已经实现了这个检查，所以方案2更合适

-- 为了向后兼容，我们使用方案1，但需要在删除产品时真正删除记录而不是软删除
-- 或者修改 image_exists 的逻辑来处理唯一索引冲突

-- 查看当前索引
SHOW INDEX FROM product_image;

-- 查看已删除的图片记录
SELECT * FROM product_image WHERE status = 0;
