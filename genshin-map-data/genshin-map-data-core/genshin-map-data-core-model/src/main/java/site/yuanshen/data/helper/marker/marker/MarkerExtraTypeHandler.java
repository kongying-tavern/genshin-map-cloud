package site.yuanshen.data.helper.marker.marker;

import com.alibaba.fastjson2.JSON;
import site.yuanshen.data.vo.adapter.marker.marker.MarkerExtraVo;
import site.yuanshen.handler.MBPJsonAbstractTypeHandler;

public class MarkerExtraTypeHandler extends MBPJsonAbstractTypeHandler<MarkerExtraVo> {
    @Override
    protected MarkerExtraVo parser(String jsonValue) {
        return JSON.parseObject(jsonValue, MarkerExtraVo.class, readFeatures);
    }
}
