-- ============================================================
-- 演示数据（可重复执行：先清空再插入，用变量取自增 ID 保证引用正确）
-- 执行方式：mysql -uroot -p < demo-data.sql
-- ============================================================

USE wiki;

DELETE FROM content;
DELETE FROM doc;
DELETE FROM ebook;
DELETE FROM category;
DELETE FROM ebook_snapshot;

-- 分类：编程技术 → Java / 数据库
INSERT INTO category (parent, name, sort) VALUES (0, '编程技术', 1);
SET @c1 = LAST_INSERT_ID();
INSERT INTO category (parent, name, sort) VALUES (@c1, 'Java', 1);
SET @c2 = LAST_INSERT_ID();
INSERT INTO category (parent, name, sort) VALUES (@c1, '数据库', 2);

-- 电子书
INSERT INTO ebook (name, category1_id, category2_id, description, cover)
VALUES ('Java 核心技术', @c1, @c2, 'Java 入门经典，从环境搭建到语法基础。', '/static/image/cover/java.jpg');
SET @ebookId = LAST_INSERT_ID();

-- 文档（含子文档）
INSERT INTO doc (ebook_id, parent, name, sort, view_count, vote_count)
VALUES (@ebookId, 0, '第一章 环境搭建', 1, 100, 10);
SET @doc1 = LAST_INSERT_ID();
INSERT INTO content (id, content) VALUES (@doc1, '<h2>第一章 环境搭建</h2><p>安装 JDK 与 IDE，配置开发环境。</p>');

INSERT INTO doc (ebook_id, parent, name, sort, view_count, vote_count)
VALUES (@ebookId, 0, '第二章 语法基础', 2, 200, 20);
SET @doc2 = LAST_INSERT_ID();
INSERT INTO content (id, content) VALUES (@doc2, '<h2>第二章 语法基础</h2><p>变量、类型与运算符。</p>');

INSERT INTO doc (ebook_id, parent, name, sort, view_count, vote_count)
VALUES (@ebookId, @doc2, '2.1 变量与类型', 1, 50, 5);
SET @doc3 = LAST_INSERT_ID();
INSERT INTO content (id, content) VALUES (@doc3, '<h2>2.1 变量与类型</h2><p>基本数据类型与变量声明。</p>');

-- 昨日快照（模拟）
INSERT INTO ebook_snapshot (ebook_id, `date`, view_count, vote_count, view_increase, vote_increase)
VALUES (@ebookId, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 250, 25, 250, 25);

-- 聚合电子书统计（对应 EbookMapper.aggregateStats）
UPDATE ebook e
LEFT JOIN (
    SELECT ebook_id, COUNT(*) AS doc_count,
           COALESCE(SUM(view_count), 0) AS view_count,
           COALESCE(SUM(vote_count), 0) AS vote_count
    FROM doc GROUP BY ebook_id
) s ON e.id = s.ebook_id
SET e.doc_count = COALESCE(s.doc_count, 0),
    e.view_count = COALESCE(s.view_count, 0),
    e.vote_count = COALESCE(s.vote_count, 0);

-- 生成今日快照（对比昨日增量）
INSERT INTO ebook_snapshot (ebook_id, `date`, view_count, vote_count, view_increase, vote_increase)
SELECT e.id, CURDATE(), e.view_count, e.vote_count,
       e.view_count - COALESCE(y.view_count, 0),
       e.vote_count - COALESCE(y.vote_count, 0)
FROM ebook e
LEFT JOIN ebook_snapshot y ON y.ebook_id = e.id AND y.`date` = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
WHERE NOT EXISTS (SELECT 1 FROM ebook_snapshot s WHERE s.ebook_id = e.id AND s.`date` = CURDATE());
