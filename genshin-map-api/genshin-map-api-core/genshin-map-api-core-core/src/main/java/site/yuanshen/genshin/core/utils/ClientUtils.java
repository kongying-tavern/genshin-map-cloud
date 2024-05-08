package site.yuanshen.genshin.core.utils;

import cn.hutool.extra.servlet.ServletUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 客户端辅助方法
 *
 * @author Alex Fang
 */
public class ClientUtils {
    /**
     * 获取客户端IPv4
     */
    public static String getClientIpv4(String nullIp) {
        String ipv4 = nullIp;
        ServletRequestAttributes servletRequestAttributes;
        if (Objects.nonNull(servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes())) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            ipv4 = ServletUtil.getClientIP(request);
        }
        return ipv4;
    }

    /**
     * 获取客户端UA
     */
    public static String getClientUa() {
        String ua = "";
        ServletRequestAttributes servletRequestAttributes;
        if (Objects.nonNull(servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes())) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            ua = ServletUtil.getHeader(request, "User-Agent", StandardCharsets.UTF_8);
        }
        return ua;
    }
}
