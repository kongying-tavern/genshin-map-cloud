package site.yuanshen.genshin.core.dsp.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

/**
 * dsp.* 配置绑定（数据防护）。
 *
 * <p>这里用 {@link Value} 而不是 {@code @ConfigurationProperties}，是为了让「配置没绑上」
 * 在启动时就暴露：注解里的 key 必须与 yml 字面一致，而 {@code dsp.auth-server.url} 与
 * {@code dsp.signing.secret-key} 不写默认值 —— 这两项缺失或名字拼错，应用直接起不来
 * （Could not resolve placeholder），而不是等到第一次签票才抛异常。
 *
 * <p>代价是字段必须平铺：{@link Value} 只对 Spring 管理的 bean 生效，
 * {@code new} 出来的嵌套对象不会走属性注入。所以这里没有 cookie / signing 这样的子对象，
 * 分组改由字段名表达。
 *
 * <p>另一处要知道的边界：{@link Value} 拦不住「配了但不可用」。空串、短于 32 字节的密钥、
 * 量级写错的有效期都能安静地通过注入，所以 {@link #validate()} 在启动期把它们逐个判掉 ——
 * 理由和「不给默认值」是同一条：宁可启动失败，不要让应用跑起来了却静默降级。
 *
 * <p>默认值的取舍只有一条标准：<b>漏配之后行为是收敛还是放宽</b>。
 * 漏配后比预期更严格（拒绝、只在本机生效、更短的存活）→ 可以给默认值；
 * 漏配后更宽松、或静默失效（更长有效期、跳过校验、把项目约定写死）→ 不给默认值，
 * 让应用在启动期直接失败（Could not resolve placeholder），
 * 不留「跑起来了但票据比预期活得久」这种状态。
 *
 * <p>按这条标准：{@code secure} / {@code http-only} / {@code same-site} 的默认值本身
 * 就在安全侧，{@code path} 不是安全边界（攻击者自行构造请求不受它约束），所以保留默认；
 * 而 {@code prefix} 是项目约定、{@code expiry-seconds} 是安全参数，一律不给默认值。
 * 票据寿命只由 {@code expiry-seconds} 这一个旋钮决定 —— Cookie 的 Max-Age 直接取它的值，
 * 不再有第二个可能与之冲突的参数。
 */
@Data
@Component
public class DspProperties {

    /**
     * 校验服务地址。它会被原样写进 base Cookie，边缘拿它与自己的白名单做精确匹配。
     *
     * <p>它不在待签串里，所以白名单是它唯一的防线：白名单只应有一条，且必须与配置
     * 精确相等，不能用「前缀包含」或宽松正则 —— 宽松匹配等于把边缘节点变成跳板。
     */
    @Value("${dsp.auth-server.url}")
    private String authServerUrl;

    /**
     * Cookie 名前缀。这是项目自己的约定，按环境在配置里给。
     *
     * <p>不给默认值：写死在代码里等于把「这是哪个项目」一起写死，而且它是两端对齐
     * Cookie 名的唯一依据 —— 前缀配错的表现是「票据签了但边缘一个都认不出来」。
     */
    @Value("${dsp.cookie.prefix}")
    private String cookiePrefix;

    /**
     * Cookie 作用域。
     *
     * <p>票据是要发到 CDN 上去用的，所以只要 CDN 与本站不是同一个 host（本项目的常态），
     * 就必须配成两者的共同父域，例如 {@code .example.com}。留空意味着 Cookie 只绑在本站的
     * host 上，浏览器不会把它带给 CDN —— 表现为「登录成功了，但边缘看到的请求永远没有票」。
     *
     * <p>唯一可以留空的情形是纯 localhost 联调：带 Domain 的 Cookie 会被浏览器直接丢弃。
     * 那是「本机且不做域名映射」时的特例，不是通用建议。
     */
    @Value("${dsp.cookie.domain:}")
    private String cookieDomain;

    @Value("${dsp.cookie.path:/}")
    private String cookiePath;

    @Value("${dsp.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${dsp.cookie.http-only:true}")
    private boolean cookieHttpOnly;

    /**
     * 仅当 CDN 域名与本站在同一个站点（同一 eTLD+1）时才可用 Lax。
     * 若 CDN 是独立域名，跨站子资源请求需要 None + Secure，而浏览器正在淘汰
     * 第三方 Cookie，那种情况下这个方案本身就需要重新评估。
     */
    @Value("${dsp.cookie.same-site:Lax}")
    private String cookieSameSite;

    /**
     * 密钥最小长度（字节）。HMAC-SHA256 的密钥短于摘要长度会削弱安全性，而密钥过短不会有
     * 任何报错 —— 短的照样签得出、照样验得过，只在强度上静默变差，所以必须显式挡住。
     */
    public static final int MIN_SECRET_KEY_BYTES = 32;

    /**
     * 签发端密钥，边缘侧持有同一份。轮换时在边缘同时留新旧两把、逐个尝试，
     * 待旧票据全部过期再摘除旧密钥 —— payload 里不带密钥版本，不需要 kid。
     *
     * <p>长度不得短于 {@link #MIN_SECRET_KEY_BYTES} 字节，由启动期校验把关。
     */
    @Value("${dsp.signing.secret-key}")
    private String signingSecretKey;

    /**
     * 票据寿命上限（秒）= 365 天。
     *
     * <p>它不是业务需要，是给 {@code expiry-seconds} 的「单位写错」兜底：填成毫秒量级
     * （如 2592000000，即 30 天写成了毫秒）时，算出来的 exp 仍是一个合法的秒级数值
     * （≈ 公元 2108 年），payload 那侧的秒级值域判别抓不到 —— 只有时长这一层能看出
     * 「这个有效期长得不像话」。两者问的不是同一个问题，都要有。
     */
    public static final long MAX_EXPIRY_SECONDS = 31_536_000L;

    /**
     * 票据有效期（秒），决定 payload 的 exp。
     *
     * <p>它同时就是 Cookie 的 Max-Age —— 两者取同一个值，所以这里是控制票据寿命的
     * 唯一旋钮（不再单独配 cookie.max-age）。
     *
     * <p>不给默认值：它是安全参数，漏配必须是启动失败，而不是静默按某个长值签发。
     *
     * <p><b>约束一：不得小于 access_token 的有效期（本项目恒为 1800 秒，硬编码在
     * {@code JwtAccessToken}）。</b> 否则会出现「票先过期、token 还活着」—— 前端收不到 401、
     * 没有理由去刷新，用户就卡在 403 上直到 token 自然过期。
     *
     * <p><b>约束二：建议取 token 有效期的两倍及以上（1800 → 3600）。</b> 票的存活要扛得住
     * 「前端只在收到 401 时才刷新」这种最迟的刷新时机 —— 那一刻正好是 token 到期的同一瞬，
     * 票若同时到期就成了踩线（成败取决于网络延迟）。取两倍即留出整整一个刷新周期的余量，
     * 不必关心前端用哪种刷新策略。代价是爬虫手工保存 Cookie 后的可用窗口同比放大。
     *
     * <p><b>上限见 {@link #MAX_EXPIRY_SECONDS}，由启动期校验把关。</b>
     */
    @Value("${dsp.signing.expiry-seconds}")
    private long signingExpirySeconds;

    /**
     * 启动期校验「配了但不可用」的那部分。
     *
     * <p>必须在启动期判，不能留到签发时：签发失败会被 {@code DspTokenCookieFilter} 接住、只记
     * 一条 warn，登录照常成功 —— 结果是「所有人都拿不到票」这件事只出现在日志里。这与「不给
     * 默认值」是同一条取舍：宁可启动失败，也不要跑起来了却静默降级。
     */
    @PostConstruct
    void validate() {
        requireNonEmpty(authServerUrl, "dsp.auth-server.url");
        requireNonEmpty(cookiePrefix, "dsp.cookie.prefix");

        int keyBytes = signingSecretKey == null ? 0 : signingSecretKey.getBytes(StandardCharsets.UTF_8).length;
        if (keyBytes < MIN_SECRET_KEY_BYTES) {
            throw new IllegalStateException(
                "dsp.signing.secret-key 至少要 " + MIN_SECRET_KEY_BYTES
                    + " 字节（HMAC-SHA256 的密钥长度），当前为 " + keyBytes + " 字节"
            );
        }

        if (signingExpirySeconds <= 0 || signingExpirySeconds > MAX_EXPIRY_SECONDS) {
            throw new IllegalStateException(
                "dsp.signing.expiry-seconds 必须在 1 ~ " + MAX_EXPIRY_SECONDS
                    + " 秒之间，当前为 " + signingExpirySeconds + "（超出多半是把秒写成了毫秒）"
            );
        }
    }

    private static void requireNonEmpty(String value, String key) {
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException(key + " 不能为空");
        }
    }

    /**
     * 拼出 Cookie 名。「前缀 + 字段名」这个约定只在这里出现一处，边缘侧对齐时只看它。
     */
    public String cookieName(String field) {
        return cookiePrefix + field;
    }
}
