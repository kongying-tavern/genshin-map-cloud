package site.yuanshen.common.web.utils;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class JsonUtils {
    public static Map<String, Object> merge(String jsonString, Map<String, Object> config) {
        Map<String, Object> jsonObj = jsonToMap(jsonString);
        return merge(jsonObj, config);
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
