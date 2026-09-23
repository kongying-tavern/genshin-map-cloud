package site.yuanshen.genshin.core.dsp.protocol;

/**
 * DSP 协议常量与待签串构造。
 *
 * <p>两段待签串：
 * <ul>
 *   <li><b>第一段</b>（签发端 → 边缘）就是 payload 本身，见 {@link DspPayload}。它不含换行，
 *       所以 {@code Sign = HMAC-SHA256(key, <Payload cookie 的值>)} —— 可以用 openssl 手工
 *       复现，签名对不上时不必读代码。</li>
 *   <li><b>第二段</b>（边缘 → 校验段）是 payload 与 timestamp 的拼接，见 {@link #edgeSignMessage}。</li>
 * </ul>
 *
 * <p>两段签名不可互换，由「结构不相交」保证，而不是由前缀保证：第一段待签串含 0 个 \n，
 * 第二段含 1 个，任何串都不可能同时属于两者 —— 即使两把密钥被误配成同一个值。这条保证
 * 完全落在第二段的 timestamp 上：它一旦被删，第二段就退化成与第一段同形的单字段串，
 * 隔离只剩「两把密钥不同」一层。
 *
 * <p><b>上述不变式的前提是 payload 里不允许出现 \n</b>，由 {@link DspPayload} 的整串正则承担。
 * 将来放宽该正则（比如给 uid 加后缀、或引入自由文本字段）等于同时拆掉两段之间的隔离，
 * 改动时必须连同这条一起重新评估。
 *
 * <p>拼接统一用 \n 而不是 &amp; ：payload 自身就是 uid=..&amp;exp=.. 这种 k=v 串，再用 &amp;
 * 拼接会和它自己的分隔符混在一起；而 payload 与 timestamp 都是可变长字段，直接拼接会产生
 * 歧义串 —— payload="uid=1&amp;exp=2" 配 ts="34" 与 payload="uid=1&amp;exp=23" 配 ts="4"
 * 拼出同一个 "uid=1&amp;exp=234"，于是同一个签名能被不同的 payload/ts 组合复用。
 * \n 不在 payload 正则与数字的字符集里，切分点唯一。
 *
 * <p>协议里没有版本位。遇到「形状相同但语义变了」的变更（例如 exp 从秒改成毫秒），用
 * 「重置 Cookie 前缀」或「改字段名」隔离 —— 前者让浏览器根本不会带上旧票，后者让旧票的正则
 * 直接不匹配。两者都是发布动作，不必在待签串里长期占一个字节。
 */
public final class DspProtocol {

    private DspProtocol() {
    }

    /**
     * Cookie 字段名后缀，实际名字由 dsp.cookie.prefix 拼出。
     */
    public static final String FIELD_BASE = "Base";

    public static final String FIELD_SIGN = "Sign";

    public static final String FIELD_PAYLOAD = "Payload";

    /**
     * 第二段待签串：边缘用自己的令牌签，回调校验段时带上。
     *
     * <p>只签 payload 与 timestamp 两个字段，判据是「校验段会不会读它」。校验端只回答
     * 「这张票现在能不能取资源」—— 一档结论，放行或拒绝，不按请求路径做范围判定，所以
     * 请求的 resource 不入签；clientIp 目前没有读取方，也不入签。将来若出现要据此拒绝的
     * 判断（例如同一 uid 从大量 IP 访问），再把对应字段加回来，那是两端同发的协议变更。
     *
     * <p>timestamp 收原始字符串而不是 long：签名覆盖的是边缘实际写下的那串字节，先 parse
     * 再 toString 等于隐含假设「边缘一定用规范数字形式」（前导零、正号都会破坏它），而这个
     * 假设不在协议文本里。收原始串既消除它，也让「parse 失败」变成验签之后才需要面对的事 ——
     * 验签只做字符串拼接与 HMAC，不解析任何未认证的输入。
     *
     * @throws IllegalArgumentException 任一字段为 null。{@code String.join} 会把 null 写成
     *         字面量 {@code "null"}，两端串不一致，而且只在缺值时出错、最难排查
     */
    public static String edgeSignMessage(String payload, String timestampRaw) {
        if (payload == null || timestampRaw == null) {
            throw new IllegalArgumentException("待签字段不能为 null：String.join 会把 null 写成字面量 \"null\"");
        }
        return String.join("\n", payload, timestampRaw);
    }
}
