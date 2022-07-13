package site.yuanshen.genshin.core.convert;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Lists;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.enums.HistoryType;

import java.util.List;

public class HistoryConvert {
    private static final JsonMapper MAPPER = new JsonMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    /**
     * 新转换模块需要在此插入
     */
    private static final List<Convert> converts = Lists.newArrayList(

    );

    public static History convert(Object source) {
        return converts.stream().filter(convert -> convert.support(source)).findAny()
                .orElseThrow(() -> new RuntimeException(source.getClass() + " not support")).convert(source);
    }

    public static Object reConvert(History history, HistoryType type) {
        return converts.stream().filter(convert -> convert.support(type)).findAny()
                .orElseThrow(() -> new RuntimeException(type + " not support")).reConvert(history);
    }


}

// 转换模块示例
//private static class AreaConvert extends DefaultConvert {
//	private static final JsonMapper mapper = MAPPER;
//
//	@Override
//	public boolean support(Object o) {
//		return o instanceof Area || (o instanceof HistoryType && getType().equals(o));
//	}
//
//	@Override
//	@SneakyThrows
//	public Object reConvert(History history) {
//		return mapper.readValue(history.getContent(), Area.class);
//	}
//
//	@Override
//	HistoryType getType() {
//		return HistoryType.AREA;
//	}
//
//	@Override
//	@SneakyThrows
//	Pair<String, Long> getContentAndId(Object o) {
//		Area area = (Area) o;
//		area.setVersion(null);
//		String content = mapper.writeValueAsString(area);
//		Long id = area.getId();
//		return Pair.of(content, id);
//	}
//}
