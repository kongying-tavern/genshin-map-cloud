package site.yuanshen.data.helper;

import com.alibaba.fastjson2.JSON;
import site.yuanshen.data.vo.adapter.marker.linkage.PathEdgeVo;
import site.yuanshen.handler.MBPJsonAbstractTypeHandler;

import java.util.List;

public class MarkerLinkagePathTypeHandler extends MBPJsonAbstractTypeHandler<List<PathEdgeVo>> {
    @Override
    protected List<PathEdgeVo> parser(String jsonValue) {
        return JSON.parseArray(jsonValue, PathEdgeVo.class, readFeatures);
    }
}
