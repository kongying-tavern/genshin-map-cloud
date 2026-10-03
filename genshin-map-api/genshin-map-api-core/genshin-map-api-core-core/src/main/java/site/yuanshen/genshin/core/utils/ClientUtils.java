package site.yuanshen.genshin.core.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
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
     * 客户端信息数据结构
     */
    @Data
    public static class ClientInfo {
        String ipv4 = "";

        String ua = "";
    }

    /**
     * 客户端信息生成配置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @With
    public static class ClientConfig {
        public static ClientConfig create() {
            return new ClientConfig();
        }

        private String ipv4Default = "N/A";

        private int uaMaxLength = 500;
    }

    /**
     * 获取客户端信息
     */
    public static ClientInfo getClientInfo(ClientInfo info, ClientConfig config) {
        return getClientInfo(null, info, config);
    }

    /**
     * 获取客户端信息（显式指定请求）
     *
     * <p>Servlet 过滤器链上排在 RequestContextFilter（order = -105）之前的过滤器拿不到
     * RequestContextHolder，也就没有「当前请求」可读，ipv4 会落成 "N/A"。这类调用方用本
     * 重载把手上已有的请求传进来；传 null 时行为与 {@link #getClientInfo(ClientInfo, ClientConfig)} 一致。
     */
    public static ClientInfo getClientInfo(HttpServletRequest request, ClientInfo info, ClientConfig config) {
        if (info == null) {
            info = new ClientInfo();
        }
        if (config == null) {
            config = ClientConfig.create();
        }

        if (StrUtil.isBlank(info.getIpv4())) {
            final String ipv4 = ClientUtils.getClientIpv4(request, config.getIpv4Default());
            info.setIpv4(ipv4);
        }
        if (StrUtil.isBlank(info.getUa())) {
            final String ua = StrUtil.sub(ClientUtils.getClientUa(request), 0, config.getUaMaxLength());
            info.setUa(ua);
        }

        return info;
    }

    /**
     * 获取客户端IPv4
     */
    public static String getClientIpv4(String nullIp) {
        return getClientIpv4(null, nullIp);
    }

    /**
     * 获取客户端IPv4（显式指定请求，见 {@link #getClientInfo(HttpServletRequest, ClientInfo, ClientConfig)}）
     */
    public static String getClientIpv4(HttpServletRequest request, String nullIp) {
        String ipv4 = nullIp;
        final String[] headers = new String[] { "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR" };
        final Function<String, Boolean> ipv4Test = (String ipStr) -> {
            if (StrUtil.isBlank(ipStr))
                return false;
            List<String> ipChunks = StrUtil.split(ipStr, ".");
            if (ipChunks.size() != 4)
                return false;
            for (String chunk : ipChunks) {
                try {
                    int chunkNum = Integer.parseInt(chunk, 10);
                    if (chunkNum < 0 || chunkNum > 255)
                        return false;
                } catch (Exception e) {
                    return false;
                }
            }
            return true;
        };
        final HttpServletRequest target = resolveRequest(request);
        if (Objects.nonNull(target)) {
            for (int i = 0; i < headers.length; i++) {
                ipv4 = ServletUtil.getClientIPByHeader(target, headers[i]);
                if (ipv4Test.apply(ipv4))
                    break;
            }
        }
        return ipv4;
    }

    /**
     * 获取客户端UA
     */
    public static String getClientUa() {
        return getClientUa(null);
    }

    /**
     * 获取客户端UA（显式指定请求，见 {@link #getClientInfo(HttpServletRequest, ClientInfo, ClientConfig)}）
     */
    public static String getClientUa(HttpServletRequest request) {
        String ua = "";
        final HttpServletRequest target = resolveRequest(request);
        if (Objects.nonNull(target)) {
            ua = ServletUtil.getHeader(target, "User-Agent", StandardCharsets.UTF_8);
        }
        return ua;
    }

    /**
     * 解析出本次调用要用的请求：显式传入的优先，否则取 RequestContextHolder 里的当前请求
     */
    private static HttpServletRequest resolveRequest(HttpServletRequest request) {
        if (Objects.nonNull(request)) {
            return request;
        }
        final ServletRequestAttributes servletRequestAttributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return Objects.isNull(servletRequestAttributes) ? null : servletRequestAttributes.getRequest();
    }
}
