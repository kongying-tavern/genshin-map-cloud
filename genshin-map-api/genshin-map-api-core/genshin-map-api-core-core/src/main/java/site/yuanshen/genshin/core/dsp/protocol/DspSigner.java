package site.yuanshen.genshin.core.dsp.protocol;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * HMAC-SHA256 与 base64url 编解码。
 *
 * <p>base64url 一律不带 = 填充：边缘函数侧是手写编码器（V8 里没有 Buffer），
 * 两边必须产出完全相同的字符串。
 */
public final class DspSigner {

    private DspSigner() {
    }

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    public static String sign(String secretKey, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return ENCODER.encodeToString(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA256 计算失败", e);
        }
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
