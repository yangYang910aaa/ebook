package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 数字电子书系统启动类
 * - @SpringBootApplication：自动配置 + 组件扫描
 * - @EnableScheduling：开启定时任务支持（StatsScheduledTask）
 */
@SpringBootApplication
@EnableScheduling
public class EBookApplication {

	public static void main(String[] args) {
		SpringApplication.run(EBookApplication.class, args);
	}

}
