package site.yuanshen.common.core.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class JsonUtils {

    public static final JSONReader.Feature[] defaultReadFeatures = new JSONReader.Feature[]{
        JSONReader.Feature.UseBigDecimalForFloats,
        JSONReader.Feature.UseBigDecimalForDoubles,
        JSONReader.Feature.UseNativeObject
    };

    public static final JSONWriter.Feature[] defaultWriteFeatures = new JSONWriter.Feature[]{
        JSONWriter.Feature.BrowserCompatible,
        JSONWriter.Feature.WriteEnumUsingToString,
        JSONWriter.Feature.WriteBigDecimalAsPlain,
        JSONWriter.Feature.WriteEnumUsingToString,
        JSONWriter.Feature.WriteNonStringKeyAsString
    };

    /**
     * 修补合并 JSON，新数据中为 null 的键会被删除，其余值会被替换
     *
     * @param oldJsonStr 旧Json数据，需要填补的原始数据
     * @param newJsonStr 新Json数据，填补的数据
     * @return 填补完成的Json
     */
    public static <K, V> String merge(String oldJsonStr, String newJsonStr) {
        Map<K, V> oldJsonObj = jsonToMap(oldJsonStr);
        Map<K, V> newJsonObj = jsonToMap(newJsonStr);
        return JSON.toJSONString(merge(oldJsonObj, newJsonObj), defaultWriteFeatures);
    }

    public static <T> T merge(T oldJsonObject, T newJsonObject, Class<T> clazz) {
        final Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            final String key = field.getName();
            final Object val = BeanUtil.getFieldValue(newJsonObject, key);
            BeanUtil.setFieldValue(oldJsonObject, key, val);
        }

        return oldJsonObject;
    }

    public static <K, V> Map<K, V> merge(Map<K, V> oldJsonObject, Map<K, V> newJsonObject) {
        Map<K, V> oldJsonObj = jsonToMap(oldJsonObject);
        Map<K, V> newJsonObj = jsonToMap(newJsonObject);

        for (Map.Entry<K, V> newJsonEntry : newJsonObj.entrySet()) {
            K key = newJsonEntry.getKey();
            V val = newJsonEntry.getValue();

            if (val == null) {
                oldJsonObj.remove(key);
            } else {
                oldJsonObj.put(key, val);
            }
        }

        return oldJsonObj;
    }

    public static <K, V> Map<K, V> jsonToMap(Map<K, V> jsonObject) {
        Map<K, V> jsonObj = new HashMap<>();

        if (jsonObject == null) {
            return jsonObj;
        }
        return jsonObject;
    }

    public static <K, V> Map<K, V> jsonToMap(String jsonString) {
        Map<K, V> jsonObj = new HashMap<>();

        if (StrUtil.isBlank(jsonString)) {
            jsonString = "{}";
        }

        try {
            jsonObj = (Map<K, V>) JSON.parseObject(jsonString, Map.class, defaultReadFeatures);
        } catch (Exception e) {
            // do nothing
        }

        return jsonObj;
    }

    public static <T> T jsonToObject(T obj, Class<T> clazz) {
        T jsonObject = null;
        try {
            jsonObject = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }

        try {
            jsonObject = (T) JSON.parseObject(JSON.toJSONString(obj), clazz, defaultReadFeatures);
        } catch (Exception e) {
            // do nothing
        }
        return jsonObject;
    }

    public static <T> T jsonToObject(String jsonString, Class<T> clazz) {
        try {
            return (T) JSON.parseObject(jsonString, clazz, defaultReadFeatures);
        } catch (Exception e) {
            return null;
        }
    }
}
