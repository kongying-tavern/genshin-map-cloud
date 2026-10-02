package site.yuanshen.genshin.core.dsp.protocol;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
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
