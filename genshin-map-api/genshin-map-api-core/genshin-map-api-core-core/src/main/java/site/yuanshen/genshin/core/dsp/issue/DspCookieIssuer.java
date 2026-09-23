package site.yuanshen.genshin.core.dsp.issue;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import site.yuanshen.genshin.core.dsp.config.DspProperties;
import site.yuanshen.genshin.core.dsp.protocol.DspPayload;
import site.yuanshen.genshin.core.dsp.protocol.DspProtocol;
import site.yuanshen.genshin.core.dsp.protocol.DspSigner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 签发端：把一张 CDN 访问票据写成三个 Set-Cookie 头。
 *
 * <p>票据只承载 uid 与 exp，不承载资源范围 —— 「哪些路径需要验票」由 CDN 的路径规则决定，
 * 「这张票现在还能不能用」由校验段每次回调现判，两者都不写进票据。范围不在票据里的好处是
 * 它不会被票据一起复制走：爬虫拿到票，也只能以这个 uid 的身份去问校验段，而校验段读的是
 * 服务端当前状态 —— 封禁、登出不必等票据过期就生效。
 *
 * <p>职责边界 —— 这里只负责「签」。防爬虫的实际效果取决于调用方在这之前的动作：额度扣减、
 * 频率限制、人机校验。签名本身拦不住能跑 JS 的爬虫，它只是把「直接爬」变成「先拿票」。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DspCookieIssuer {

    private final DspProperties dspProperties;

    @Data
    @Builder
    public static class Issued {

        private List<String> setCookieHeaders;

        private DspPayload payload;

        private String signature;
    }

    /**
     * @param uid 用户标识，取自 JWT 的 userId
     */
    public Issued issue(long uid) {
        // 权威检查在 DspProperties.validate()：启动期一次判掉空值、密钥长度、有效期上限。
        // 这里再判一遍是为了「绕过 Spring 直接 new 出来的实例」也挡得住 —— 签发失败会被
        // 过滤器降级成一条 warn，是整条链路上最不该靠日志兜底的地方，多一道判断比少一道便宜。
        String base = dspProperties.getAuthServerUrl();
        if (base == null || base.isEmpty()) {
            throw new IllegalStateException("dsp.auth-server.url 为空，无法签发 DSP Cookie");
        }

        String secretKey = dspProperties.getSigningSecretKey();
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException("dsp.signing.secret-key 为空，无法签发 DSP Cookie");
        }

        // 它同时是 Cookie 的 Max-Age，所以这个守卫也顺带保住了「浏览器不会当场丢弃 Cookie」
        long expirySeconds = dspProperties.getSigningExpirySeconds();
        if (expirySeconds <= 0) {
            throw new IllegalStateException("dsp.signing.expiry-seconds 必须为正数，否则签出来的票立即过期");
        }

        long expireAt = Instant.now().getEpochSecond() + expirySeconds;
        DspPayload payload = DspPayload.of(uid, expireAt);
        String payloadText = payload.serialize();
        // 第一段待签串就是 payload 本身（没有前缀，见 DspProtocol 类注释）：
        // Sign = HMAC-SHA256(secretKey, <Payload cookie 的值>)，可用 openssl 手工复现
        String signature = DspSigner.sign(secretKey, payloadText);

        List<String> headers = new ArrayList<>(3);
        headers.add(buildSetCookie(dspProperties.cookieName(DspProtocol.FIELD_BASE), base));
        headers.add(buildSetCookie(dspProperties.cookieName(DspProtocol.FIELD_SIGN), signature));
        headers.add(buildSetCookie(dspProperties.cookieName(DspProtocol.FIELD_PAYLOAD), payloadText));

        log.debug("已签发 DSP Cookie: uid={}, exp={}", uid, expireAt);

        return Issued.builder()
            .setCookieHeaders(headers)
            .payload(payload)
            .signature(signature)
            .build();
    }

    private String buildSetCookie(String name, String value) {
        String domain = dspProperties.getCookieDomain();
        String path = dspProperties.getCookiePath();
        String sameSite = dspProperties.getCookieSameSite();
        // Max-Age 与签名寿命取同一个值，浏览器与边缘对「这张票还能用多久」始终一致：
        // 既不会出现「Cookie 还在、签名已过期」的死票，也不会出现「签名还有效、Cookie 已先被丢弃」
        long maxAge = dspProperties.getSigningExpirySeconds();

        ResponseCookie.ResponseCookieBuilder cookie = ResponseCookie.from(name, value)
            .path(path == null || path.isEmpty() ? "/" : path)
            .maxAge(maxAge)
            .secure(dspProperties.isCookieSecure())
            .httpOnly(dspProperties.isCookieHttpOnly());
        if (domain != null && !domain.isEmpty()) {
            cookie.domain(domain);
        }
        if (sameSite != null && !sameSite.isEmpty()) {
            cookie.sameSite(sameSite);
        }
        // 属性顺序与拼接由 ResponseCookie 负责；它会连带输出 Expires（按 maxAge 现算），
        // 浏览器以 Max-Age 为准，Expires 是给不认识 Max-Age 的老客户端的兜底。
        return cookie.build().toString();
    }
}
