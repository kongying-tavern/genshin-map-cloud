package site.yuanshen.data.helper.marker.tweak;

import site.yuanshen.data.dto.MarkerDto;
import site.yuanshen.data.enums.marker.tweak.TweakHandlerEnum;
import site.yuanshen.data.vo.adapter.marker.tweak.TweakConfigMetaVo;
import site.yuanshen.data.vo.adapter.marker.tweak.TweakConfigVo;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class MarkerTweakDataHelper {
    public static MarkerDto applyTweakRules(MarkerDto markerDto, List<TweakConfigVo> tweakRules) {
        if(markerDto == null || tweakRules == null) {
            return markerDto;
        }

        for(TweakConfigVo tweakRule : tweakRules) {
            applyTweakRule(markerDto, tweakRule);
        }

        return markerDto;
    }

    public static <T> void applyTweakRule(MarkerDto markerDto, TweakConfigVo tweakRule) {
        if(markerDto == null || tweakRule == null) {
            return;
        }

        final TweakHandlerEnum tweakHandler = TweakHandlerEnum.find(tweakRule.getProp(), tweakRule.getType());
        if(tweakHandler == null) return;
        final Function<MarkerDto, T> valueGetter = (Function<MarkerDto, T>) tweakHandler.getProp().getGetter();
        final BiConsumer<MarkerDto, T> valueSetter = (BiConsumer<MarkerDto, T>) tweakHandler.getProp().getSetter();
        if(valueGetter == null || valueSetter == null) return;

        // Get value
        T value = valueGetter.apply(markerDto);
        if(value == null) return;

        // Transform value
        BiFunction<T, TweakConfigMetaVo, T> valueTransformer = (BiFunction<T, TweakConfigMetaVo, T>) tweakHandler.getType().getTransformer();
        value = valueTransformer.apply(value, tweakRule.getMeta());

        // Set value back
        valueSetter.accept(markerDto, value);
    }
}
