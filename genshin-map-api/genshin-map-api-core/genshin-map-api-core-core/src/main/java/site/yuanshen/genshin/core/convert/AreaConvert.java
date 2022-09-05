package site.yuanshen.genshin.core.convert;

import cn.hutool.core.lang.Pair;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import site.yuanshen.data.entity.Area;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.enums.HistoryType;

import static site.yuanshen.genshin.core.convert.HistoryConvert.MAPPER;

//转换模块示例(暂不需要)
@Deprecated
public class AreaConvert extends DefaultConvert {
	private static final JsonMapper mapper = MAPPER;

	@Override
	public boolean support(Object o) {
		return o instanceof Area || (o instanceof HistoryType && getType().equals(o));
	}

	@Override
	@SneakyThrows
	public Object reConvert(History history) {
		return mapper.readValue(history.getContent(), Area.class);
	}

	@Override
	HistoryType getType() {
		return HistoryType.AREA;
	}

	@Override
	@SneakyThrows
    Pair<String, Long> getContentAndId(Object o) {
		Area area = (Area) o;
		area.setVersion(null);
		String content = mapper.writeValueAsString(area);
		Long id = area.getId();
		return Pair.of(content, id);
	}
}