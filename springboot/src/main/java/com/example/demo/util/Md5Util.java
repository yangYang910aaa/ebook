package com.example.demo.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * MD5 工具：后端统一对收到的密码做 MD5 存储/校验。
 * 前端先加密一次（MD5(password)）再提交，即可形成文档要求的"前端+后端双重加密"。
 */
public class Md5Util {

    // 私有构造：工具类禁止实例化
    private Md5Util() {
    }

    /**
     * 计算字符串的 MD5 哈希值（32位小写十六进制）
     * 用于密码加密：前端先 MD5 一次提交，后端再 MD5 一次存储，形成双重加密
     *
     * @param input 原始字符串
     * @return MD5 十六进制字符串
     */
    public static String md5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            // 将字节数组转为两位十六进制字符串（不足两位补前导0）
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xff);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("MD5 计算失败", e);
        }
    }
}
