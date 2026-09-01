-- ============================================================
-- 初始数据（种子数据）
-- 执行方式：mysql -uroot -p < seed.sql
-- ============================================================

-- 初始管理员账号：admin / 123456（密码为 MD5(123456)）
INSERT IGNORE INTO wiki.`user` (login_name, name, password)
VALUES ('admin', '管理员', 'e10adc3949ba59abbe56e057f20f883e');
