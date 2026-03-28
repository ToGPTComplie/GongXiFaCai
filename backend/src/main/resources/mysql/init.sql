-- 如果数据库不存在则创建
CREATE DATABASE IF NOT EXISTS portfolio_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户并设置密码（兼容 MySQL 8.0+）
CREATE USER IF NOT EXISTS 'gongxifacai'@'%' IDENTIFIED BY 'HongBaoNaLai888+';

-- 授予该用户对该数据库的所有权限
GRANT ALL PRIVILEGES ON portfolio_db.* TO 'gongxifacai'@'%';

-- 刷新权限
FLUSH PRIVILEGES;
