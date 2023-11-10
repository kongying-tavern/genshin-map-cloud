package site.yuanshen.handler;

import com.alibaba.fastjson2.JSON;

import java.util.List;

public class MBPJsonArrayTypeHandler extends MBPJsonAbstractTypeHandler<List<Object>> {
    @Override
    protected List<Object> parser(String jsonValue) {
        return JSON.parseArray(jsonValue, readFeatures);
    }
}
