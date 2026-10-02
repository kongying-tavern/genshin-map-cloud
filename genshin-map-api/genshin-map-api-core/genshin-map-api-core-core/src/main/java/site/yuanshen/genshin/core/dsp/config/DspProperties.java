package site.yuanshen.genshin.core.dsp.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * <p><b>默认值一概不给 —— 每一项都必须由配置显式给出。</b>理由是「漏配」与「刻意配成某个值」
 * 两种情形必须可区分：一旦给了默认值，配错了的人在配置里什么也看不出来，而系统会安静地
 * 跑在一个他没打算用的形态上（例如票据比预期活得久、Cookie 不带 Domain 却没人知道）。
 * 不给默认值的代价是漏配即启动失败（Could not resolve placeholder），而这个「起步就炸」
 * 正是想要的 —— 它比运行期的静默降级便宜得多。
 *
 * <p>唯一的例外是<b>布尔安全开关</b>（{@code secure} / {@code http-only}）：它们的默认值取
 * 在<b>严格侧</b>（{@code true}），漏配的结果是收紧而不是放开，跑不起来的是「本就不该跑」的
 * 那种不安全部署。它们也不承载部署拓扑信息 —— 不像 {@code domain} / {@code same-site} 那样
 * 「正确答案取决于环境」，所以没有「默认值猜错了」这一类问题。
 *
 * <p>{@code prefix} 是项目约定、{@code expiry-seconds} 是安全参数，两者都属于「正确答案取决于
 * 环境」的那一类，同样不给默认值。
 * 票据寿命只由 {@code expiry-seconds} 这一个旋钮决定 —— Cookie 的 Max-Age 直接取它的值，
 * 不再有第二个可能与之冲突的参数。
 */
@Slf4j
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
     *
     * <p>空串是<b>有意义的值</b>（不写 Domain），但那必须来自显式配置 —— 这里不给默认值，
     * 漏配就启动失败。否则「忘了配」和「刻意留空」在结果上长得一模一样，而两者的意图相反。
     */
    @Value("${dsp.cookie.domain}")
    private String cookieDomain;

    /**
     * Cookie 路径。不给默认值：路径该是多少取决于部署形态，写死 {@code /} 会让「某处配了别的路径」
     * 这件事无处可查。
     */
    @Value("${dsp.cookie.path}")
    private String cookiePath;

    /**
     * 仅在 https 下回传（localhost 例外）；明文 http 部署要显式配 false。
     *
     * <p>这一项保留默认值 {@code true}，属于下一节说的例外：它是布尔开关，默认取的是<b>严格侧</b>。
     */
    @Value("${dsp.cookie.secure:true}")
    private boolean cookieSecure;

    /**
     * 禁止 JS 读取。保留默认值 {@code true} —— 见类注释里关于「哪一类可以有默认值」的说明。
     */
    @Value("${dsp.cookie.http-only:true}")
    private boolean cookieHttpOnly;

    /**
     * 仅当 CDN 域名与本站在同一个站点（同一 eTLD+1）时才可用 Lax。
     * 若 CDN 是独立域名，跨站子资源请求需要 None + Secure，而浏览器正在淘汰
     * 第三方 Cookie，那种情况下这个方案本身就需要重新评估。
     *
     * <p>不给默认值：SameSite 取决于 CDN 与本站在不在同一个站点，那是部署拓扑决定的，
     * 猜出来的默认值只有在恰好同站时才对 —— 而猜错的表现是所有票据到不了边缘。
     */
    @Value("${dsp.cookie.same-site}")
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

    // ---------- 校验接口侧（回流复核） ----------
    //
    // 上面那一组是【签发端】用的；下面这一组是本服务作为【校验接口】用的。
    // 两种角色可以同时存在于同一个进程，但配置必须分开：签发密钥与边缘令牌
    // 是两把不同的密钥，混在一起配等于把「两段不可互换」那层隔离拆掉。
    //
    // 这一组不在这里用 @Value 声明，而在 {@link DspEdgeProperties} 里 —— 为了让 yml 能写成
    // 数组形式。这是本类「字段必须平铺」那条约束唯一的例外：它不是嵌套对象，
    // 而是一个独立注册、由构造器注入进来的 bean，属性绑定照样生效。

    private final DspEdgeProperties edge;

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
        // cookie.domain 有意不判非空 —— 空串是它的合法取值（不写 Domain）。
        // 但 path 与 same-site 空了就没有合理解释：Cookie Path 为空等于随便哪里都匹配不上，
        // SameSite 为空则是无效值，两者都只可能是漏配或占位符没渲染出来
        requireNonEmpty(cookiePath, "dsp.cookie.path");
        requireNonEmpty(cookieSameSite, "dsp.cookie.same-site");

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

        // 校验接口侧。这一组的字段不在本类中，所以「漏配」不会表现为占位符解析失败，
        // 而是表现为这里判出来的空表 / 非正值 —— 报错变成显式校验，而不是绑定期异常
        List<String> tokens = edge.getTokens();
        // 这两项没有默认值，所以用包装类型：只有这样「未配置」与「配了 0」才分得开 ——
        // ticket-tolerance 的合法取值里本来就有 0（不容差），给默认值就等于默认不容差
        long callbackWindowSeconds = requireConfigured(
            edge.getCallbackWindowSeconds(), "dsp.edge.callback-window-seconds");
        long ticketToleranceSeconds = requireConfigured(
            edge.getTicketToleranceSeconds(), "dsp.edge.ticket-tolerance-seconds");

        validateEdgeTokens(tokens);
        // 槽位数没有「正确值」可校验 —— 它必须与边缘侧的 DSP_EDGE_SLOT_COUNT 相等，
        // 而那一边的值本服务看不到。能做的只有启动时把它打出来：两侧不一致的表现
        // 是「某些边缘的回流全部验不过」，排障时第一个要核对的就是这个数。
        // 只打数字不打令牌：槽位数不是秘密，令牌是。
        log.info(
            "DSP 校验接口就绪: slotCount={}（槽位数 = dsp.edge.tokens 条目数），"
                + "边缘侧 DSP_EDGE_SLOT_COUNT 必须与之相等",
            tokens.size()
        );
        if (callbackWindowSeconds <= 0) {
            throw new IllegalStateException(
                "dsp.edge.callback-window-seconds 必须为正数，当前为 " + callbackWindowSeconds
            );
        }
        if (ticketToleranceSeconds < 0) {
            throw new IllegalStateException(
                "dsp.edge.ticket-tolerance-seconds 不能为负，当前为 " + ticketToleranceSeconds
            );
        }
        if (ticketToleranceSeconds > signingExpirySeconds) {
            throw new IllegalStateException(
                "dsp.edge.ticket-tolerance-seconds（" + ticketToleranceSeconds
                    + "）不应大于 dsp.signing.expiry-seconds（" + signingExpirySeconds
                    + "）—— 容差接近票的寿命，等于没人再管这张票能用多久"
            );
        }
    }

    /**
     * 令牌表逐槽检查：每个槽非空、互不重复、不带首尾空白、且长度达标。
     *
     * <p>槽位数由表的长度决定（不是单独配置的），所以「表配全了」就等于「槽全填实了」——
     * 但那只是长度相等，内容仍可能不可用：任一槽为空串会让落到该槽的 kid 永远验不过，
     * 而这类空缺运行时极难发现 —— 症状是「某个边缘突然全被拒」，根因却在一份看起来
     * 配全了的配置里。让它起不来，比排障便宜得多。
     *
     * <p>重复令牌同样要挡：模型的前提是「每个边缘各持一把、互不相同」，两把相同等于
     * 把两个边缘绑成同一个身份，独立吊销 / 轮换就失效了。
     */
    private void validateEdgeTokens(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalStateException(
                "dsp.edge.tokens 不能为空 —— 槽位数就是它的条目数，空表等于没有任何可用槽位"
            );
        }
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token == null || token.isBlank()) {
                throw new IllegalStateException(
                    "dsp.edge.tokens[" + i + "] 为空 —— 空槽会让落在这个槽的 kid 永远验不过"
                );
            }
            if (!seen.add(token)) {
                throw new IllegalStateException(
                    "dsp.edge.tokens[" + i + "] 与前面的槽重复 —— "
                        + "令牌互不相同是「单个边缘泄露不影响其余」的前提"
                );
            }
            // 空白先用 isBlank 判「整条为空」，再判「有内容但带首尾空白」：
            // 后者过得了非空检查，长度也多半达标，唯一后果是它与边缘那把令牌逐字节不等 ——
            // 症状是该槽位的回流全部被判签名不匹配，而两边各自看配置都像是配对了
            if (!token.equals(token.strip())) {
                throw new IllegalStateException(
                    "dsp.edge.tokens[" + i + "] 含首尾空白 —— yml 的引号会把空白算进令牌本身，"
                        + "请去掉引号内的空格或改用不含空格的令牌"
                );
            }
            int bytes = token.getBytes(StandardCharsets.UTF_8).length;
            if (bytes < MIN_SECRET_KEY_BYTES) {
                throw new IllegalStateException(
                    "dsp.edge.tokens[" + i + "] 只有 " + bytes + " 字节，至少要 "
                        + MIN_SECRET_KEY_BYTES + " 字节（HMAC-SHA256 的密钥长度）"
                );
            }
        }
    }

    private static void requireNonEmpty(String value, String key) {
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException(key + " 不能为空");
        }
    }

    /**
     * 没有默认值的数值项：不能被省略，也不能是占位符没渲染出来的残留。
     *
     * <p>{@code @Value} 的必填项缺配会直接抛占位符解析失败，但 {@code dsp.edge.*} 走的是
     * {@link DspEdgeProperties} 的按名绑定 —— 缺配时那里只是一个静默的 null。这里把它
     * 补成同等的启动期失败，并给出比「数字不合法」更贴近根因的消息。
     */
    private static long requireConfigured(Long value, String key) {
        if (value == null) {
            throw new IllegalStateException(
                key + " 未配置 —— 这一项没有默认值，必须由配置显式给出"
            );
        }
        return value;
    }

    /**
     * 拼出对外名字。「前缀 + 字段名」这个约定只在这里出现一处，边缘侧对齐时只看它。
     *
     * <p>将来若要引入 DSP 的响应头，名字同样走这里 —— 不写死 {@code X-DSP-} 之类的固定串，
     * 因为它必须与边缘侧同源：边缘读的也是同一份前缀拼出来的名字，写死就多一处漂移。
     */
    public String cookieName(String field) {
        return cookiePrefix + field;
    }

    // ---------- 校验接口侧 ----------
    //
    // 下面几个都只是转发到 edge —— 配置的实际声明在 {@link DspEdgeProperties}。
    // 留转发是为了让调用方不必知道「这一组和上面那些不在同一个类里」这件事，
    // 也避免哪天把 edge 提升为独立 bean 时改动扩散到 Controller。

    /**
     * 槽位数（= 令牌表的长度）。边缘侧的 {@code DSP_EDGE_SLOT_COUNT} 必须与它一致 ——
     * 边缘凭它把 kid 随机化成 {@code base + n·slotCount}，两边不一致会选错槽位。
     *
     * <p>它就是从 {@code tokens.size()} 数出来的，没有第二个来源 —— 详见
     * {@link DspEdgeProperties#getTokens()} 里关于「不要给它单开配置项」的说明。
     *
     * <p>表的顺序即槽位顺序，所以「往表尾追加」才是加边缘的正确方式 ——
     * 插在中间会把后面所有边缘的槽位整体挪一位。
     */
    public int edgeSlotCount() {
        return edge.getTokens().size();
    }

    /**
     * 按槽位取令牌。
     *
     * <p>槽位由调用方用 {@code slot = kid mod slotCount} 算好再传进来 —— 取模只在一处做，
     * 避免「谁负责取模」在两边各写一遍、改了一处漏了另一处。
     */
    public String edgeToken(int slot) {
        return edge.getTokens().get(slot);
    }

    public long getEdgeCallbackWindowSeconds() {
        return edge.getCallbackWindowSeconds();
    }

    public long getEdgeTicketToleranceSeconds() {
        return edge.getTicketToleranceSeconds();
    }
}
