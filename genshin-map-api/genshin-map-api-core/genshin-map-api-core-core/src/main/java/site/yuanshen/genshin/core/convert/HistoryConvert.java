package site.yuanshen.genshin.core.convert;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Lists;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.enums.HistoryEditType;
import site.yuanshen.data.enums.HistoryType;

import java.util.List;
import java.util.stream.Collectors;

public class HistoryConvert {
    static final JsonMapper MAPPER = new JsonMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    /**
     * 新转换模块需要在此插入
     */
    private static final List<Convert> converts = Lists.newArrayList(
            new AreaConvert(),
            new ItemConvert(),
            new MarkerConvert()
    );

    public static History convert(Object source, HistoryEditType editType) {
        return converts.stream().filter(convert -> convert.support(source)).findAny()
                .orElseThrow(() -> new RuntimeException(source.getClass() + " not support")).convert(source, editType);
    }


    public static Object reConvert(History history, HistoryType type) {
        return converts.stream().filter(convert -> convert.support(type)).findAny()
                .orElseThrow(() -> new RuntimeException(type + " not support")).reConvert(history);
    }


}


