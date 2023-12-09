package site.yuanshen.genshin.core.convert;

import site.yuanshen.data.entity.History;
import site.yuanshen.data.enums.HistoryEditType;

public interface Convert {
    boolean support(Object o);

    History convert(Object o, HistoryEditType editType);

    Object reConvert(History history);
}
