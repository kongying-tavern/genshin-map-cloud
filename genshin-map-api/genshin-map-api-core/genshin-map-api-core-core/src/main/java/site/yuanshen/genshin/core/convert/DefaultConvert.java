package site.yuanshen.genshin.core.convert;

import cn.hutool.core.lang.Pair;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.servlet.ServletUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.enums.HistoryEditType;
import site.yuanshen.data.enums.HistoryType;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

public abstract class DefaultConvert implements Convert {

    @Override
    public final History convert(Object o, HistoryEditType editType) {
        History history = new History();

        String ipv4 = "N/A";
        ServletRequestAttributes servletRequestAttributes;
        if (Objects.nonNull(servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes())) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            ipv4 = ServletUtil.getClientIP(request);
        }

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
