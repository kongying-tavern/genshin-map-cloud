package site.yuanshen.genshin.core.dsp.protocol;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * HMAC-SHA256 与 hex 编解码（DSP 签名）。
 *
 * <p>DSP 签名统一用小写 hex：只有一种字母表、无填充、无大小写歧义，签发端 / 验证端 /
 * 校验接口三方用任意语言实现都不可能写错而不立刻全部验签失败。身份令牌（JWT）的编解码
 * 受规范约束仍走 base64url，见 {@link #DECODER} 与 {@link #decodeBase64Url}，不在此列。
 */
public final class DspSigner {

    private DspSigner() {
    }

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    /**
     * 计算 DSP 签名，返回【小写 hex】字符串（32 字节 → 64 字符，无填充、无大小写歧义）。
     *
     * <p>这与验证端 {@code dsp_protocol.lua} 的 {@code hmac_hex}、以及校验接口第二段签名共用
     * 同一种字符集 —— 任意一端改了编码都必须三方同改同上线，否则所有合法票会被拒。身份令牌
     * （JWT）仍走 base64url，见 {@link #encodeBase64Url} / {@link #decodeBase64Url}，与本文无关。
     */
    public static String sign(String secretKey, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return toHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA256 计算失败", e);
        }
    }

    /**
     * RFC 4231 第 4.1.1 节（HMAC-SHA256）测试向量的期望值：key 是 20 个 0x0b、data 是
     * "Hi There"。硬编码在这里不构成密钥管理问题 —— 这两个输入是公开常量。
     */
    private static final String RFC4231_VECTOR_1 =
        "b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7";

    /**
     * 启动期自检：拿上面那个公开测试向量算一次 HMAC-SHA256，对不上就拒绝启动。
     *
     * <p>它挡的不是「算法库坏了」—— {@code HmacSHA256} 在 JCE 里太老，坏掉的情况几乎不存在。
     * 它挡的是「这台机器上 mac.init 之后悄悄走了另一条路径」这种看不出症状的偏差：
     * Provider 被换过、容器里打了补丁、某次 JDK 升级改了默认值 —— 症状都是同一个，
     * 上线之后所有票签得不对、所有回流验不过，而且线上日志只会写「签名不一致」，
     * 指向「两把密钥配错了」这个最常见的猜测。跑一遍已知答案，把这一整片排查空间
     * 在启动期一次性消掉。
     *
     * <p>与边缘侧（Lua）那次启动期自检是同一条设计：算法接错了必须表现成「起不来」，
     * 而不是表现成上线之后的全站 403。
     *
     * @throws IllegalStateException 算出来的结果与测试向量不一致时
     */
    public static void selfTest() {
        char[] key = new char[20];
        // 0x0b / 20 个：写成 (char) 11 而不是 '\u000b'，后者编译前会被替换成真正的控制字符，源码里留一个不可见的东西不是想让人读到的。
        Arrays.fill(key, (char) 11);
        String actual = sign(new String(key), "Hi There");
        if (!actual.equals(RFC4231_VECTOR_1)) {
            throw new IllegalStateException(
                "DSP HMAC-SHA256 启动自检失败：拿 RFC 4231 的测试向量算出 " + actual
                    + "，期望 " + RFC4231_VECTOR_1
                    + " —— 这台机器上的 HmacSHA256 实现与预期不一致，签出来的票边缘一律验不过"
            );
        }
    }

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    private static String toHex(byte[] data) {
        char[] out = new char[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            int v = data[i] & 0xFF;
            out[i * 2] = HEX_DIGITS[v >>> 4];
            out[i * 2 + 1] = HEX_DIGITS[v & 0x0F];
        }
        return new String(out);
    }

    /**
     * 常量时间比较。用 equals 会在首个不同字节处提前返回，攻击者可按字节逐位爆破签名。
     */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(
            a.getBytes(StandardCharsets.US_ASCII),
            b.getBytes(StandardCharsets.US_ASCII)
        );
    }

    public static String encodeBase64Url(String plain) {
        return ENCODER.encodeToString(plain.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeBase64Url(String encoded) {
        return new String(DECODER.decode(encoded), StandardCharsets.UTF_8);
    }

    public static String randomToken(int byteLength) {
        byte[] buffer = new byte[byteLength];
        new SecureRandom().nextBytes(buffer);
        return ENCODER.encodeToString(buffer);
    }
}
