package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 电子书配置（封面上传）
 */
@Data
@Component
@ConfigurationProperties(prefix = "ebook")
public class EbookProperties {

    /** 封面保存目录（前端静态目录） */
    private String coverDir;

    /** 封面访问前缀 */
    private String coverUrlPrefix;
}
