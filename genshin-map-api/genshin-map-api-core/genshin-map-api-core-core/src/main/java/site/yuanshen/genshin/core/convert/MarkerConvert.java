package site.yuanshen.genshin.core.convert;

import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import site.yuanshen.data.dto.ItemDto;
import site.yuanshen.data.dto.MarkerDto;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.enums.HistoryType;

public class MarkerConvert extends DefaultConvert{

    @Override
    public boolean support(Object o) {
        return o instanceof MarkerDto || (o instanceof HistoryType && getType().equals(o));
    }

    @Override
    @SneakyThrows
    public Object reConvert(History history) {
        return JSONObject.parseObject(history.getContent(), MarkerDto.class).getVo();
    }

    @Override
    HistoryType getType() {
        return HistoryType.MARKER;
    }

    @Override
    @SneakyThrows
    Pair<String, Long> getContentAndId(Object o) {
        MarkerDto markerDto = (MarkerDto) o;
        markerDto.setVersion(null);
        String content= JSON.toJSONString(markerDto);
        Long id = markerDto.getId();
        return Pair.of(content, id);
    }

}
