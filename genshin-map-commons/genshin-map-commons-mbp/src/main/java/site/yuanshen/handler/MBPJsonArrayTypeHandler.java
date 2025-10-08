package site.yuanshen.handler;

import com.alibaba.fastjson2.JSON;

import java.util.List;

public class MBPJsonArrayTypeHandler<V> extends MBPJsonAbstractTypeHandler<List<V>> {
    @Override
    protected List<V> parser(String jsonValue) {
        try {
            return (List<V>) JSON.parseArray(jsonValue, readFeatures);
        } catch (Exception e) {
            return List.of();
        }
    }
}
