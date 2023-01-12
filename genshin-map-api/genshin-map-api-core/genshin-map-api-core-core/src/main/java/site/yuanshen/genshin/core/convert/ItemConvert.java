package site.yuanshen.genshin.core.convert;

import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import site.yuanshen.data.dto.ItemDto;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.enums.HistoryType;


public class ItemConvert extends DefaultConvert{

    @Override
    public boolean support(Object o) {
        return o instanceof ItemDto || (o instanceof HistoryType && getType().equals(o));
    }

    @Override
    @SneakyThrows
    public Object reConvert(History history) {=
        return JSONObject.parseObject(history.getContent(), ItemDto.class).getVo();
    }

    @Override
    HistoryType getType() {
        return HistoryType.ITEM;
    }

    @Override
    @SneakyThrows
    Pair<String, Long> getContentAndId(Object o) {
        ItemDto item = (ItemDto) o;
        item.setVersion(null);
        String content = JSON.toJSONString(item);
        Long id = item.getItemId();
        return Pair.of(content, id);
    }
}
