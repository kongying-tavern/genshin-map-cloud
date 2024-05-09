package site.yuanshen.genshin.core.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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
        final ServletRequestAttributes servletRequestAttributes;
        final String[] headers = new String[]{"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        final Function<String, Boolean> ipv4Test = (String ipStr) -> {
            if(StrUtil.isBlank(ipStr)) return false;
            List<String> ipChunks = StrUtil.split(ipStr, ".");
            if(ipChunks.size() != 4) return false;
            for(String chunk : ipChunks) {
                try {
                    int chunkNum = Integer.parseInt(chunk, 10);
                    if(chunkNum < 0 || chunkNum > 255) return false;
                } catch (Exception e) {
                    return false;
                }
            }
            return true;
        };
        if (Objects.nonNull(servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes())) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            for(int i = 0; i < headers.length; i++) {
                ipv4 = ServletUtil.getClientIPByHeader(request, headers[i]);
                if(ipv4Test.apply(ipv4))
                    break;
            }
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
