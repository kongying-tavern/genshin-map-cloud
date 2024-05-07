package site.yuanshen.genshin.core.convert;

import cn.hutool.core.lang.Pair;
import cn.hutool.crypto.SecureUtil;
import site.yuanshen.genshin.core.utils.ClientUtils;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.enums.HistoryEditType;
import site.yuanshen.data.enums.HistoryType;

public abstract class DefaultConvert implements Convert {

    @Override
    public History convert(Object o, HistoryEditType editType) {
        History history = new History();

        String ipv4 = ClientUtils.getClientIpv4("N/A");

        Pair<String, Long> contentAndId = getContentAndId(o);
        String content = contentAndId.getKey();
        Long tId = contentAndId.getValue();
        String md5 = SecureUtil.md5(content);

        history.setTId(tId);
        history.setContent(content);
        history.setMd5(md5);
        history.setType(getType().getCode());
        history.setIpv4(ipv4);
        history.setEditType(editType);
        return history;
    }


    abstract HistoryType getType();

    abstract Pair<String, Long> getContentAndId(Object o);
}
