package com.example.demo.util;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 对象拷贝工具（属性名一致的 Bean 拷贝）
 */
public class CopyUtil {

    // 私有构造：工具类禁止实例化
    private CopyUtil() {
    }

    /**
     * 单个对象拷贝：通过反射创建目标对象，按属性名复制（属性名需一致）
     * 用于 Entity → DTO / DTO → Entity 等场景
     *
     * @param source      源对象
     * @param targetClass 目标类型
     * @return 目标对象（source 为 null 时返回 null）
     */
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

    /**
     * 列表批量拷贝：遍历源列表逐个 copy，用于分页查询结果的类型转换
     *
     * @param sourceList  源对象列表
     * @param targetClass 目标元素类型
     * @return 目标对象列表（sourceList 为 null 时返回空列表）
     */
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
