package com.example.demo.util;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 对象拷贝工具（属性名一致的 Bean 拷贝）
 */
public class CopyUtil {

    private CopyUtil() {
    }

    public static <T> T copy(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        T target;
        try {
            target = targetClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("创建目标对象失败: " + targetClass.getName(), e);
        }
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T> List<T> copyList(List<?> sourceList, Class<T> targetClass) {
        List<T> list = new ArrayList<>();
        if (sourceList == null) {
            return list;
        }
        for (Object source : sourceList) {
            list.add(copy(source, targetClass));
        }
        return list;
    }
}
