package site.yuanshen.genshin.core.convert;

import site.yuanshen.data.entity.History;

public interface Convert {
    boolean support(Object o);

    History convert(Object o);

    Object reConvert(History history);
}