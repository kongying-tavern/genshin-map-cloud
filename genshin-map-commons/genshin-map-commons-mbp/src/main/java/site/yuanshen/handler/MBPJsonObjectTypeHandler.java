package site.yuanshen.handler;

import com.alibaba.fastjson2.JSON;

import java.util.Map;

public class MBPJsonObjectTypeHandler extends MBPJsonAbstractTypeHandler<Map<String, Object>> {
    @Override
    protected Map<String, Object> parser(String jsonValue) {
        return JSON.parseObject(jsonValue, readFeatures);
    }
}
