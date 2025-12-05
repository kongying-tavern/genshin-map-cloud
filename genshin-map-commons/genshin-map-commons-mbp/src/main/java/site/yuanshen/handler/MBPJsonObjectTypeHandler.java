package site.yuanshen.handler;

import com.alibaba.fastjson2.JSON;

import java.util.HashMap;
import java.util.Map;

public class MBPJsonObjectTypeHandler<K, V> extends MBPJsonAbstractTypeHandler<Map<K, V>> {
    @Override
    protected Map<K, V> parser(String jsonValue) {
        try {
            return (Map<K, V>) JSON.parseObject(jsonValue, readFeatures);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
