package site.yuanshen.genshin.core.dsp.issue;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import site.yuanshen.genshin.core.service.SysActionLogService;
import site.yuanshen.genshin.core.utils.ClientUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录换 token 时顺带下发 DSP Cookie。
 *
 * <p>挂在 /oauth/token 的成功响应上：登录成功 → 拿到 JWT → 从 JWT 里取出 userId →
 * 签一张 CDN 访问票塞回响应。客户端无需任何额外调用，前端在原有 Cookie 之外多了两个。
 *
 * <p><b>执行顺序很关键</b>：本过滤器必须排在 springSecurityFilterChain 之前。
 * Spring Security 的过滤器链在 order = -100，处理完 /oauth/token 后直接写出响应、
 * 不会再向下传递，所以排在它之后的普通过滤器根本看不到这个请求。
 * 因此这里显式声明 @Order(-200)。
 *
 * <p>签发失败只记日志、不影响登录：拿不到 CDN 票是降级，登录失败是事故。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(DspTokenCookieFilter.FILTER_ORDER)
public class DspTokenCookieFilter extends OncePerRequestFilter {

    /**
     * 必须小于 springSecurityFilterChain 的 -100，原因见类注释。
     */
    static final int FILTER_ORDER = -200;

    private static final String TOKEN_ENDPOINT = "/oauth/token";

    private static final int MAX_BODY_LENGTH = 64 * 1024;

    /**
     * 下发 DSP Cookie 的操作日志动作名。
     */
    static final String ACTION_DSP_ISSUE = "DSP_ISSUE";

    private final DspCookieIssuer dspCookieIssuer;

    private final SysActionLogService sysActionLogService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !TOKEN_ENDPOINT.equals(request.getRequestURI())
            || !"POST".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(request, wrapper);
            issueCookies(request, wrapper);
        } catch (Exception e) {
            log.warn("DSP Cookie 签发失败，已跳过，不影响本次登录", e);
        } finally {
            // 缓存体必须回写，否则客户端拿到空响应
            wrapper.copyBodyToResponse();
        }
    }

    private void issueCookies(HttpServletRequest request, ContentCachingResponseWrapper wrapper) {
        if (wrapper.getStatus() != HttpStatus.OK.value()) {
            return;
        }

        byte[] content = wrapper.getContentAsByteArray();
        if (content.length == 0 || content.length > MAX_BODY_LENGTH) {
            return;
        }

        JSONObject tokenResponse;
        try {
            tokenResponse = JSON.parseObject(new String(content, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.debug("token 响应不是合法 JSON，跳过 DSP Cookie 签发");
            return;
        }
        if (tokenResponse == null) {
            return;
        }

        String accessToken = tokenResponse.getString("access_token");
        if (accessToken == null || accessToken.isEmpty()) {
            return;
        }

        JSONObject claims = decodeJwtClaims(accessToken);
        if (claims == null) {
            return;
        }

        Long userId = claims.getLong("userId");
        if (userId == null) {
            log.debug("token 无 userId 声明（客户端模式），跳过 DSP Cookie 签发");
            return;
        }

        DspCookieIssuer.Issued issued = dspCookieIssuer.issue(userId);
        for (String setCookie : issued.getSetCookieHeaders()) {
            wrapper.addHeader(HttpHeaders.SET_COOKIE, setCookie);
        }
        log.info("已为用户 {} 下发 DSP Cookie，有效期至 {}", userId, issued.getPayload().getExpireAt());
        recordIssueAction(request, userId, issued);
    }

    /**
     * 记一条 {@code DSP_ISSUE} 操作日志，留下「谁在什么时候拿到了哪张票」。
     *
     * <p>把请求显式传给 {@link ClientUtils}：本过滤器排在 RequestContextFilter（order = -105）之前，
     * 过滤器链上还没有「当前请求」，不显式传的话 ipv4 会记成 "N/A"。
     *
     * <p>只在签发成功后调用。写日志失败只告警 —— 票已经签好也塞进响应头了，不该因为日志表
     * 不可用就让这次登录失败（与签发失败的处理同理：那是降级，这不是事故）。
     */
    private void recordIssueAction(HttpServletRequest request, long userId, DspCookieIssuer.Issued issued) {
        try {
            Map<String, Object> extraData = new HashMap<>(4);
            extraData.put("sign", issued.getSignature());
            extraData.put("payload", issued.getPayload().serialize());
            sysActionLogService.addNewLog(
                userId, ACTION_DSP_ISSUE, false, extraData,
                ClientUtils.getClientInfo(request, null, null)
            );
        } catch (Exception e) {
            log.warn("DSP 签发操作日志写入失败，不影响本次登录", e);
        }
    }

    /**
     * 只解析载荷段，不做签名校验 —— 这个 token 是本次请求刚由本服务签发的，
     * 这里只是读出身份用于签票，不依赖它做任何安全判定。
     */
    private static JSONObject decodeJwtClaims(String jwt) {
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) {
            return null;
        }
        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return JSON.parseObject(payload);
        } catch (Exception e) {
            return null;
        }
    }
}
