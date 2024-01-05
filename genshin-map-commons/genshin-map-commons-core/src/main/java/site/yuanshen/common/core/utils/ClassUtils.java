package site.yuanshen.common.core.utils;

import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.StrUtil;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassUtils {
    /**
     * 获取类字段的属性描述器
     * @param clazz 需要处理的类
     * @param propName 属性名
     * @return 属性描述器
     */
    public static <T> PropertyDescriptor getPropertyDescriptor(Class<T> clazz, String propName) {
        PropertyDescriptor pd = null;
        try {
            final Field field = clazz.getDeclaredField(propName);
            if(field != null) {
                final String propNameCap = NamingCase.toPascalCase(propName);
                final String getMethodName = "get" + propNameCap;
                final Method getMethod = clazz.getDeclaredMethod(getMethodName, new Class[]{});
                final String setMethodName = "set" + propNameCap;
                final Method setMethod = clazz.getDeclaredMethod(setMethodName, new Class[]{field.getType()});
                pd = new PropertyDescriptor(propName, getMethod, setMethod);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return pd;
    }

    /**
     * 设置属性
     * @param obj 对象
     * @param pd 属性描述
     * @param value 属性值
     */
    public static <T, V> void setProperty(T obj, PropertyDescriptor pd, V value) {
        try {
            pd.getWriteMethod().invoke(obj, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 设置属性
     * @param obj 对象
     * @param propName 属性名
     * @param value 属性值
     */
    public static <T, V> void setProperty(T obj, String propName, V value) {
        final Class clazz = obj.getClass();
        final PropertyDescriptor pd = getPropertyDescriptor(clazz, propName);
        setProperty(obj, pd, value);
    }

    /**
     * 获取属性
     * @param obj 对象
     * @param pd 属性描述
     * @return 属性值
     */
    public static <T, V> V getProperty(T obj, PropertyDescriptor pd) {
        V value = null;
        try {
            value = (V) pd.getReadMethod().invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 获取属性
     * @param obj 对象
     * @param propName 属性名
     * @return 属性值
     */
    public static <T, V> V getProperty(T obj, String propName) {
        final Class clazz = obj.getClass();
        final PropertyDescriptor pd = getPropertyDescriptor(clazz, propName);
        return getProperty(obj, pd);
    }

    /**
     * 获取对象中的值，支持 Map 和数据类
     * @param item 对象
     * @param prop 值的路径
     * @return 获取到的数据，无法获取返回 null
     */
    public static Object getValue(Object item, String prop) {
        if(StrUtil.isBlank(prop)) {
            return null;
        } else if(item == null) {
            return null;
        }

        String[] propChunks = StrUtil.splitToArray(prop, '.');
        try {
            Object itemChunk = item;
            Object obj = null;
            final int propChunkSize = propChunks.length;
            if(itemChunk == null) {
                return null;
            }

            for(int i = 0; i < propChunkSize; i++) {
                final String propChunk = propChunks[i];
                if(itemChunk instanceof Map) {
                    ((Map<?, ?>) itemChunk).get(propChunk);
                } else {
                    obj = getProperty(itemChunk, propChunk);
                }

                if(obj == null) {
                    return null;
                } else {
                    itemChunk = obj;
                }
            }
            return obj;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取对象中的值，支持 Map 和数据类，通过默认值类型确定数据类型
     * @param item 对象
     * @param prop 值的路径
     * @param defaultValue 默认值
     * @return 获取到的数据，无法获取返回默认值
     */
    public static <T> T getValue(Object item, String prop, T defaultValue) {
        Object val = getValue(item, prop);
        if(val == null) {
            return defaultValue;
        }

        T obj = null;
        try {
            obj = (T) val;
        } catch (Exception e) {
            // nothing to do;
        }
        if(obj == null) {
            return defaultValue;
        }
        return obj;
    }

    /**
     * 获取类字段
     * @param clazz 类
     * @return 字段列表
     */
    public static List<Field> getFields(Class clazz) {
        return getFields(new ArrayList<>(), clazz);
    }

    private static List<Field> getFields(List<Field> list, Class clazz) {
        final Field[] fields = clazz.getDeclaredFields();
        list.addAll(List.of(fields));

        final Class superClazz = clazz.getSuperclass();
        if(superClazz != null) {
            getFields(list, superClazz);
        }
        return list;
    }
}
