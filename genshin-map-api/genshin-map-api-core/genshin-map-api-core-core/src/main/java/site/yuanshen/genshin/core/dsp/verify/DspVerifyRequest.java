package site.yuanshen.genshin.core.dsp.verify;

import lombok.Data;

/**
 * 边缘回流请求体 —— 校验接口从边缘收到的那四个字段，与 {@link DspVerifyController} 类注释里的
 * 契约逐项对应。
 *
 * <p><b>字段类型是按契约选的，不是顺手写的</b>：
 * <ul>
 *   <li>{@code payload} 是 {@code uid=1&exp=...} 这种查询串形式，不是一个 JSON 子对象 ——
 *       它是「一段被签过的字节」，任何重新解析再序列化的动作都可能改动它。</li>
 *   <li>{@code timestamp} 必须是边缘实际写下的那串字节，所以用 {@code String} 而不是
 *       {@code long}：后者会先 parse 再 toString，等于假设「边缘一定用规范数字形式」，
 *       前导零、正号都会悄悄改掉待签串，而签名是对原文签的，改一点点就验不过。</li>
 *   <li>{@code signature} 是 64 字符小写 hex —— 形态检查由 Controller 做
 *       （这里只负责把它原样带进来）。</li>
 *   <li>{@code kid} 用 {@code Long}：它不是也不参与签名，只是个编号。用包装类型是为了让
 *       「字段缺失」与「字段为 0」能区分 —— 用 {@code long} 的话两者都会变成 0。</li>
 * </ul>
 *
 * <p><b>不认识的字段忽略</b>（Spring Boot 默认关掉 {@code FAIL_ON_UNKNOWN_PROPERTIES}）：协议
 * 演进时先加字段的那一方，不该让还没升级的对面直接 403。
 *
 * <p>绑定失败（body 不是 JSON、字段类型对不上）不在这个类里处理 —— 那是 Bind 阶段的事，
 * 由 {@link DspVerifyController#handleUnreadableBody} 统一收口。
 */
@Data
public class DspVerifyRequest {

    /** 票面原文（第一段签名签的就是它），查询串形式。不得为空。 */
    private String payload;

    /** 边缘写下回流时间戳的原始字符串。原样进待签串，不得为空、不得重新格式化。 */
    private String timestamp;

    /** 第二段签名，64 字符小写 hex。形态校验在 Controller，这里不判。 */
    private String signature;

    /**
     * 边缘身份编号，非负整数。它不影响签名，只用来选令牌：
     * {@code slot = kid mod slotCount}。
     */
    private Long kid;
}
