package site.yuanshen.common.core.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;

import java.util.HashMap;
import java.util.Map;

public class JsonUtils {

    /**
     * 修补合并 JSON，新数据中为 null 的键会被删除，其余值会被替换
     * @param oldJsonStr 旧Json数据，需要填补的原始数据
     * @param newJsonStr 新Json数据，填补的数据
     * @return 填补完成的Json
     */
    public static String merge(String oldJsonStr, String newJsonStr) {
        Map<String, Object> oldJsonObj = jsonToMap(oldJsonStr);
        Map<String, Object> newJsonObj = jsonToMap(newJsonStr);
        return JSON.toJSONString(merge(oldJsonObj, newJsonObj));
    }

    public static Map<String, Object> merge(Map<String, Object> oldJsonObject, Map<String, Object> newJsonObject) {
        Map<String, Object> oldJsonObj = jsonToMap(oldJsonObject);
        Map<String, Object> newJsonObj = jsonToMap(newJsonObject);

        for(Map.Entry<String, Object> newJsonEntry : newJsonObj.entrySet()) {
            String key = newJsonEntry.getKey();
            Object val = newJsonEntry.getValue();

            if(val == null) {
                oldJsonObj.remove(key);
            } else {
                oldJsonObj.put(key, val);
            }
        }

        return oldJsonObj;
    }

    public static Map<String, Object> jsonToMap(Map<String, Object> jsonObject) {
        Map<String, Object> jsonObj = new HashMap<>();

        if(jsonObject == null) {
            return jsonObj;
        }
        return jsonObject;
    }

    public static Map<String, Object> jsonToMap(String jsonString) {
        Map<String, Object> jsonObj = new HashMap<>();

        if(StrUtil.isBlank(jsonString)) {
            jsonString = "{}";
        }

        try {
            jsonObj = JSON.parseObject(jsonString, Map.class);
        } catch (Exception e) {
            // do nothing
        }

        return jsonObj;
    }

    public static <T> T jsonToObject(String jsonString, Class<T> clazz) {
        try {
            return (T) JSON.parseObject(jsonString, clazz);
        } catch (Exception e) {
            return null;
        }
    }
}
