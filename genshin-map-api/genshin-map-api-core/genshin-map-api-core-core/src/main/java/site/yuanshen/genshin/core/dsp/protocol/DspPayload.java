package site.yuanshen.genshin.core.dsp.protocol;

import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SAP-Payload：{@code uid=<uid>&exp=<exp>}。
 *
 * <p>payload 本身就是待签串的内容，所以它只能有一个规范形态：字段顺序固定、不多不少、
 * 无空白。解析用整串正则，而不是「按 &amp; 切开逐个取值」—— 宽松解析等于把签名覆盖的
 * 范围交给攻击者决定：多塞一个字段、换个顺序、重复一次 uid，都能被「解析成功」。
 *
 * <p>exp 是秒级 Unix 时间戳。是否过期不在这里判，容差由调用方给 —— 边缘节点与签发方
 * 的时钟不会完全同步。
 *
 * <p><b>「秒写成毫秒」这类单位误用靠值域判别挡住，不靠版本位。</b>秒级时间戳在可预见的未来
 * 都远小于 {@link #MAX_PLAUSIBLE_EPOCH_SECONDS}（10^11 秒 ≈ 3169 年，即公元 5139 年），
 * 而毫秒 ≥ 10^12、微秒 ≥ 10^15、纳秒 ≥ 10^18。没有这道判断时，一个毫秒级的 exp 会让
 * 「now - tolerance > exp」恒为假 —— 也就是这张票实际上永不过期，而且是静默通过。
 * 一道上限判断就把它变成显式拒绝，比在待签串里长期占一个版本字节便宜。
 */
@Getter
public final class DspPayload {

    /**
     * exp 的可信上限（<b>不含</b>），10^11 秒 ≈ 3169 年，即公元 5139 年。
     *
     * <p>它不是「票不许活这么久」，而是「这个数只可能是秒级」的判别线。签发端只用
     * {@code now + expiry-seconds} 生成 exp，正常值都是 10 位数，这条线留了两个数量级的余量，
     * 不会因为时间往后走而误伤。取不含是为了让位数上限落在 12 位（最紧的、仍能写出本值的形态）。
     */
    public static final long MAX_PLAUSIBLE_EPOCH_SECONDS = 100_000_000_000L;

    /**
     * 规范形态：uid 最长 19 位（long 上限），exp 最长 12 位 —— 12 位是能写出
     * {@link #MAX_PLAUSIBLE_EPOCH_SECONDS} 的最小位数，所以「位数上限」与「值域上限」
     * 严格对应，不存在永远碰不到的分支。
     *
     * <p>位数是形态、值域是判据，两者不是两层防线：13 位以上的 exp 必然 ≥ 10^12，
     * 值域检查本来就抓得到。留位数只是让正则自身读得通 —— 不接受任何永远不合法的形态。
     *
     * <p>只约束形态（字段顺序、位数、无空格），不做数值归一化：{@code uid=012345} 与
     * {@code uid=12345} 数值相同、串不同，两者都能解析成功。这不是漏洞 —— 签名覆盖的是
     * 整串原文，没人能把一张票的串换成另一串，多出来的那个串只能由签发端亲手签出来。
     */
    private static final Pattern CANONICAL = Pattern.compile("^uid=([0-9]{1,19})&exp=([0-9]{1,12})$");

    private static final int MAX_LENGTH = 64;

    private final long uid;

    private final long expireAt;

    private DspPayload(long uid, long expireAt) {
        this.uid = uid;
        this.expireAt = expireAt;
    }

    public static DspPayload of(long uid, long expireAt) {
        if (uid <= 0) {
            throw new IllegalArgumentException("uid 必须为正整数");
        }
        if (expireAt <= 0) {
            throw new IllegalArgumentException("exp 必须为秒级时间戳");
        }
        if (expireAt >= MAX_PLAUSIBLE_EPOCH_SECONDS) {
            throw new IllegalArgumentException(
                "exp 超出秒级可信范围（须小于 " + MAX_PLAUSIBLE_EPOCH_SECONDS + "），疑似把秒写成了毫秒"
            );
        }
        return new DspPayload(uid, expireAt);
    }

    /**
     * @param raw payload 原文
     * @return 结构合法且 exp 在秒级可信范围内的载荷；任何一处不合法返回 null
     */
    public static DspPayload parse(String raw) {
        if (raw == null || raw.isEmpty() || raw.length() > MAX_LENGTH) {
            return null;
        }
        Matcher matcher = CANONICAL.matcher(raw);
        if (!matcher.matches()) {
            return null;
        }
        try {
            long uid = Long.parseLong(matcher.group(1));
            long expireAt = Long.parseLong(matcher.group(2));
            // 值域是判据：正则只保证形态，这里保证「这个数只可能是秒级」
            if (uid <= 0 || expireAt <= 0 || expireAt >= MAX_PLAUSIBLE_EPOCH_SECONDS) {
                return null;
            }
            return new DspPayload(uid, expireAt);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * @return 规范形态的 payload 原文，也是被签名的内容
     */
    public String serialize() {
        return "uid=" + uid + "&exp=" + expireAt;
    }

    /**
     * @param now              当前秒级时间戳
     * @param toleranceSeconds 时钟容差
     * @return 是否已过期
     */
    public boolean isExpired(long now, long toleranceSeconds) {
        return now - toleranceSeconds > expireAt;
    }
}
