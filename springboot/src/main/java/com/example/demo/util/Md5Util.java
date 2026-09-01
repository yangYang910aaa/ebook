package com.example.demo.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * MD5 工具：后端统一对收到的密码做 MD5 存储/校验。
 * 前端先加密一次（MD5(password)）再提交，即可形成文档要求的"前端+后端双重加密"。
 */
public class Md5Util {

    private Md5Util() {
    }

    public static String md5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
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
