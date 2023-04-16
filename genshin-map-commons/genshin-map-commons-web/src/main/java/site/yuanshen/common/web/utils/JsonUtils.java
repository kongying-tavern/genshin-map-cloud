package site.yuanshen.common.web.utils;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class JsonUtils {

    /**
     *
     * @param oldJsonStr 旧Json数据，需要填补的原始数据
     * @param newJsonStr 新Json数据，填补的数据
     * @return 填补完成的Json
     */
    public static String merge(String oldJsonStr, String newJsonStr) {
        Map<String, Object> oldJsonObj = jsonToMap(oldJsonStr);
        Map<String, Object> newJsonObj = jsonToMap(newJsonStr);
        return JSON.toJSONString(merge(oldJsonObj, newJsonObj));
    }

    public static Map<String, Object> merge(Map<String, Object> jsonObj, Map<String, Object> config) {
        for(Map.Entry<String, Object> conf : config.entrySet()) {
            String key = conf.getKey();
            Object val = conf.getValue();

            if(val == null) {
                jsonObj.remove(key);
            } else {
                jsonObj.put(key, val);
            }
        }

        return  jsonObj;
    }

    public static Map<String, Object> jsonToMap(String jsonString) {
        Map<String, Object> jsonObj = new HashMap<>();

        if(StringUtils.isBlank(jsonString)) {
            jsonString = "{}";
        }

        try {
            jsonObj = JSON.parseObject(jsonString, Map.class);
        } catch (Exception e) {
            // do nothing
        }

        return jsonObj;
    }
}