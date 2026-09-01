-- ============================================================
-- 数字电子书系统 建库建表脚本（依据《项目需求分析文档》6.2/6.3）
-- 执行方式：mysql -uroot -p < wiki.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS wiki DEFAULT CHARACTER SET utf8mb4;

USE wiki;

-- 分类表：两级树形，parent=0 为一级分类
CREATE TABLE IF NOT EXISTS category (
  id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  parent BIGINT       NOT NULL DEFAULT 0 COMMENT '父分类id，0为一级',
  name   VARCHAR(100) NOT NULL COMMENT '分类名称',
  sort   INT          NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (id),
  KEY idx_parent (parent)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子书分类';

-- 电子书表：归属两级分类，含统计冗余字段
CREATE TABLE IF NOT EXISTS ebook (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  name         VARCHAR(100) NOT NULL COMMENT '电子书名称',
  category1_id BIGINT       NOT NULL DEFAULT 0 COMMENT '一级分类id',
  category2_id BIGINT       NOT NULL DEFAULT 0 COMMENT '二级分类id',
  description  VARCHAR(500)          DEFAULT NULL COMMENT '描述',
  cover        VARCHAR(200)          DEFAULT NULL COMMENT '封面相对路径',
  doc_count    INT          NOT NULL DEFAULT 0 COMMENT '文档数',
  view_count   INT          NOT NULL DEFAULT 0 COMMENT '阅读数',
  vote_count   INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
  PRIMARY KEY (id),
  KEY idx_category2 (category2_id),
  KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子书';

-- 文档表：电子书下的章节，自身树形，parent=0 为根
CREATE TABLE IF NOT EXISTS doc (
  id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  ebook_id   BIGINT       NOT NULL COMMENT '所属电子书id',
  parent     BIGINT       NOT NULL DEFAULT 0 COMMENT '父文档id，0为根',
  name       VARCHAR(200) NOT NULL COMMENT '文档名称',
  sort       INT          NOT NULL DEFAULT 0 COMMENT '排序',
  view_count INT          NOT NULL DEFAULT 0 COMMENT '阅读数',
  vote_count INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
  PRIMARY KEY (id),
  KEY idx_ebook (ebook_id),
  KEY idx_parent (parent)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档（章节）';

-- 文档内容表：与 doc 1:1，主键相同
CREATE TABLE IF NOT EXISTS content (
  id      BIGINT      NOT NULL COMMENT '主键，等于doc.id',
  content MEDIUMTEXT  COMMENT '富文本正文',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档富文本内容';

-- 用户表：登录名唯一不可改，密码为 32 位 MD5 密文
CREATE TABLE IF NOT EXISTS `user` (
  id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  login_name VARCHAR(50) NOT NULL COMMENT '登录名（唯一）',
  name       VARCHAR(50) NOT NULL COMMENT '昵称',
  password   CHAR(32)    NOT NULL COMMENT 'MD5密文',
  PRIMARY KEY (id),
  UNIQUE KEY uk_login_name (login_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- 电子书每日统计快照：同一电子书同一天唯一
CREATE TABLE IF NOT EXISTS ebook_snapshot (
  id             BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  ebook_id       BIGINT NOT NULL COMMENT '电子书id',
  `date`         DATE   NOT NULL COMMENT '统计日期',
  view_count     INT    NOT NULL DEFAULT 0 COMMENT '当日总阅读量',
  vote_count     INT    NOT NULL DEFAULT 0 COMMENT '当日总点赞量',
  view_increase  INT    NOT NULL DEFAULT 0 COMMENT '阅读日增量',
  vote_increase  INT    NOT NULL DEFAULT 0 COMMENT '点赞日增量',
  PRIMARY KEY (id),
  UNIQUE KEY uk_ebook_date (ebook_id, `date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子书每日统计快照';
