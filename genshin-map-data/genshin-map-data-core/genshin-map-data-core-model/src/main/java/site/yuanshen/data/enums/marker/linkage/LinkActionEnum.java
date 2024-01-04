package site.yuanshen.data.enums.marker.linkage;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.TriConsumer;
import site.yuanshen.data.dto.adapter.marker.linkage.graph.AccumulatorCache;
import site.yuanshen.data.dto.adapter.marker.linkage.graph.AccumulatorKey;
import site.yuanshen.data.dto.adapter.marker.linkage.graph.DistributorDto;
import site.yuanshen.data.dto.adapter.marker.linkage.graph.DistributorKey;
import site.yuanshen.data.helper.marker.linkage.MarkerLinkageGraphAccumulator;
import site.yuanshen.data.helper.marker.linkage.MarkerLinkageGraphDistributor;
import site.yuanshen.data.vo.MarkerLinkageVo;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public enum LinkActionEnum implements IEnum<String> {
    TRIGGER(
            MarkerLinkageGraphAccumulator::withTrigger,
            MarkerLinkageGraphDistributor::withTrigger
    ),
    TRIGGER_ALL(
            MarkerLinkageGraphAccumulator::withTriggerAll,
            MarkerLinkageGraphDistributor::withTriggerAll
    ),
    TRIGGER_ANY(
            MarkerLinkageGraphAccumulator::withTriggerAny,
            MarkerLinkageGraphDistributor::withTriggerAny
    ),
    RELATED(
            MarkerLinkageGraphAccumulator::withRelated,
            MarkerLinkageGraphDistributor::withRelated
    ),
    EQUIVALENT(
            MarkerLinkageGraphAccumulator::withEquivalent,
            MarkerLinkageGraphDistributor::withEquivalent
    );

    @Override
    public String getValue() {
        return this.name();
    }

    @Getter
    private final BiConsumer<List<AccumulatorCache>, MarkerLinkageVo> accumulator;
    @Getter
    private final TriConsumer<Map<DistributorKey, DistributorDto>, AccumulatorKey, AccumulatorCache> distributor;

    public static LinkActionEnum find(String code) {
        if(StrUtil.isBlank(code)) return null;
        for(LinkActionEnum val : values()) {
            if(val.name().equals(code)) return val;
        }
        return null;
    }

}
