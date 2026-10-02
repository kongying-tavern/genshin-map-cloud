package site.yuanshen.genshin.core.dsp.verify;

import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import site.yuanshen.genshin.core.dsp.config.DspProperties;
import site.yuanshen.genshin.core.dsp.protocol.DspPayload;
import site.yuanshen.genshin.core.dsp.protocol.DspProtocol;
import site.yuanshen.genshin.core.dsp.protocol.DspSigner;

import java.time.Instant;
import java.util.regex.Pattern;

/**
 * 校验接口：边缘（验证端）二次签名后的回流落点。
 *
 * <p>它只信任边缘用自己槽位那把令牌签出来的东西 —— 不解析、不信任客户端带来的 Cookie
 * 原值。这是整条链路信任边界的关键：<b>校验接口对客户端零信任</b>。
 *
 * <p><b>请求体（契约）</b>：
 * <pre>{@code
 * POST /dsp/verify
 * {"payload":"uid=1&exp=1760000000","timestamp":"1760000100","signature":"<64 字符小写 hex>","kid":0}
 * }</pre>
 * 待签串是 {@code payload + "\n" + timestamp}（见 {@link DspProtocol#edgeSignMessage}）；
 * {@code kid} 是边缘身份编号、<b>不参与签名</b>，只用于选令牌：
 * {@code slot = kid mod slotCount}，槽位数 {@link DspProperties#edgeSlotCount()} 就是令牌表的
 * 长度（不要把它理解成一个可单独配置的参数）。
 *
 * <p><b>绝不能</b>把调用方递来的密钥拿去验 —— 令牌必须由本服务自己持有、按 kid 查表。
 * 否则等于把秘密交给调用方，「防直连源站」那一层意义归零。
 *
 * <p>返回 200 = 放行，403 = 拒绝。边缘按状态码办事，不做内容解析。
 *
 * <p><b>本端点不校验调用方的 IP：第二段的 HMAC 签名本身就是身份证明</b> —— 能算出合法
 * signature 的只有持有对应槽位令牌的那一方，与它从哪个地址来无关。何况经网关转发后
 * {@code getRemoteAddr()} 取到的是网关的地址，来源判断在这里也无从做起。
 *
 * <p>这条论断的前提是「本接口的输出只是一个布尔值」：它不返回数据、不改变任何状态，
 * 所以即便被任意调用，调用方也拿不到东西。将来若往 {@code businessDecision} 里塞了会按 uid
 * 泄露状态的逻辑（例如返回「该用户是否被封」），那个前提就失效了 —— 届时先补来源或认证。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DspVerifyController {

    /** 第二段签名的形态：HMAC-SHA256 的 32 字节，小写 hex，正好 64 字符 */
    private static final Pattern HEX64 = Pattern.compile("^[0-9a-f]{64}$");

    private final DspProperties props;

    @PostMapping(path = "/dsp/verify")
    public ResponseEntity<DspVerifyDenyBody> verify(
        @RequestBody DspVerifyRequest body,
        HttpServletRequest request
    ) {
        // 0. 字段齐全性。
        //    payload / timestamp 要非空串 —— 空串对下面的验签毫无意义，属于「给了但没给」；
        //    signature 只判 null：空串交给第 3 步的形态检查，落到「形态不对」那条原因上。
        String payload = body.getPayload();
        String timestampRaw = body.getTimestamp();
        String signature = body.getSignature();
        if (payload == null || payload.isEmpty()
            || timestampRaw == null || timestampRaw.isEmpty()
            || signature == null) {
            return deny(request, "缺少必填字段（payload / timestamp / signature）");
        }

        // kid：必须给了、必须非负。契约允许它超过槽位数（取模后自然落进合法槽位），
        // 也允许任意大的非负整数；负数在这里拒，而「压根不是整数」已经在绑定阶段
        // 由下面的 handleUnreadableBody 接住 —— 那里拿不到字段名，所以只能给一个总的拒因。
        Long kidValue = body.getKid();
        if (kidValue == null) {
            return deny(request, "缺少 kid");
        }
        long kid = kidValue;
        if (kid < 0) {
            return deny(request, "kid 非法（须为非负整数）");
        }

        // 1. 时间窗（防重放）。只有这一步需要把 timestamp 解析成数字
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampRaw);
        } catch (NumberFormatException e) {
            return deny(request, "timestamp 不是整数");
        }
        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - timestamp) > props.getEdgeCallbackWindowSeconds()) {
            return deny(request, "回流时间超出允许窗口（疑似重放）");
        }

        // 2. 选令牌：slot = kid mod slotCount。
        //    floorMod 而不是 %：kid 已判非负，但 % 对负数会给负槽位，
        //    一旦上面的判负被改动就会变成数组越界 —— 这里不依赖那个前提。
        int slotCount = props.edgeSlotCount();
        int slot = Math.floorMod(kid, slotCount);
        String token = props.edgeToken(slot);

        // 3. 验第二段签名。
        //    形态先查：不是正好 64 位小写 hex 的一律拒。比对本身也拦得住它们（长度不同
        //    isEqual 直接不等），但那样只会得到同一个「签名不对」 —— 排障时无法区分
        //    「边缘算错了」和「边缘压根用了另一种编码」，所以形态单独给一条原因。
        //
        //    用 timestampRaw（边缘实际写下的那串字节）而不是解析后的数字：
        //    先 parse 再 toString 等于假设「边缘一定用规范数字形式」，前导零、正号都会破坏它。
        if (!HEX64.matcher(signature).matches()) {
            return deny(request, "signature 形态不对（须为 64 字符小写 hex）");
        }
        String expected = DspSigner.sign(token, DspProtocol.edgeSignMessage(payload, timestampRaw));
        if (!DspSigner.constantTimeEquals(expected, signature)) {
            return deny(request, "第二段签名与服务端算出的不一致");
        }

        // 4. 票面结构仍然自己查一遍。边缘已验过第一段，但这里不能假设它一定传对了 ——
        //    而且 uid 是下面业务判定的输入，必须来自一个自己验证过的结构。
        DspPayload ticket = DspPayload.parse(payload);
        if (ticket == null) {
            return deny(request, "票面格式不合法（不是预期的 uid / exp 结构）");
        }

        // 5. 过期自己也判一次。边缘验第一段时已经判过，但那条「对客户端零信任」的边界
        //    对边缘同样成立：第二段签名只证明「这串 payload 是某个持有合法令牌的边缘递来的」，
        //    不证明「它现在还有效」。而且 exp 会一路流进下面的业务判定 —— 一张过期票不该
        //    有机会被判成可放行。
        //
        //    复用第 1 步那个 now：时间窗与是否过期是同一时刻的结论，取两次时间会让恰好落在
        //    边界上的票时而被拒时而被放行。
        if (ticket.isExpired(now, props.getEdgeTicketToleranceSeconds())) {
            return deny(request, "票据已过期");
        }

        // 6. 业务判定挂载点。
        //    只有这里需要「边缘拿不到的动态权限」时才值得保留这次回调 —— 登出、封禁、
        //    权限变更：这些在签票那一刻还不存在，只有业务后端知道。
        //    如果判定只用票面字段，说明这次回调是多余的。
        if (!businessDecision(ticket)) {
            return deny(request, "业务判定不通过");
        }

        return ResponseEntity.ok().header("Cache-Control", "no-store").build();
    }

    /**
     * body 不是合法 JSON、或某个字段的类型对不上时，Spring 在绑定阶段就抛这个异常 ——
     * 默认处理是回一个 400，错误体里还带着 Spring 自己的描述。这里收口成本端统一的样子：
     * <b>403 + 一句人读的话 + 一行日志</b>，与其余拒因同形。
     *
     * <p>只挂在这个 Controller 上（不是 {@code @ControllerAdvice}）：别的端点该怎么报错还怎么报，
     * 不要让一个边缘回调接口的异常处理方式扩散到全站。
     *
     * <p>代价要知道：绑定失败时不分字段，「kid 不是整数」这类错误拿不到那么细的说法，
     * 只有一个笼统的「请求体不对」。要区分就得退回逐字段解析 —— 上面选择 POJO 时
     * 已经把这一点换掉了。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<DspVerifyDenyBody> handleUnreadableBody(
        HttpMessageNotReadableException e,
        HttpServletRequest request
    ) {
        // Jackson 的消息里带字段名与期望类型，那是给排障的人看的 —— 只进日志，不原样透出去
        log.info("DSP 回流请求体无法绑定: {}", e.getMessage());
        return deny(request, "请求体不是合法 JSON，或字段类型不对");
    }

    /**
     * Content-Type 缺失或不是 JSON 时的处理。
     *
     * <p>为什么要有这一个：这类请求在 {@code @RequestBody} 绑定之前就被判掉了 —— 没有任何
     * 转换器认领它们，Spring 抛出 {@code HttpMediaTypeNotSupportedException}，默认回 415。
     * 那条路径不进本类的任何一行代码，于是「拒绝发生了」这件事在服务端留不下痕迹，
     * 边缘那边只看到一个没来由的失败。挂上这个处理器之后，它与其余拒因同形：
     * <b>403 + 一句人读的话 + 一行日志</b>。
     *
     * <p>既然这里已经收口，{@code @PostMapping} 上就不必再写
     * {@code consumes = APPLICATION_JSON_VALUE} —— 写了反而更差：{@code consumes} 是<b>匹配
     * 条件</b>，匹配不上时这个 handler 压根不会被选中，处理权直接落在框架手上，本类一行都不
     * 执行，日志里照样什么都没有。不写它则请求一定进来，由这里决定怎么拒绝。
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<DspVerifyDenyBody> handleUnsupportedMediaType(
        HttpMediaTypeNotSupportedException e,
        HttpServletRequest request
    ) {
        log.info("DSP 回流请求的 Content-Type 无法处理: {}", e.getContentType());
        return deny(request, "请求的 Content-Type 不是 application/json");
    }

    /**
     * 占位：接入你自己的权限模型（封禁名单、订阅等级、额度剩余等）。
     *
     * <p>入参只有票面：契约的回流请求体里没有 resource / clientIp，所以「按资源判定」
     * 这类逻辑要么改协议加字段（那是两端同发的协议变更），要么在签发时写进票面。
     */
    private boolean businessDecision(DspPayload ticket) {
        return true;
    }

    private ResponseEntity<DspVerifyDenyBody> deny(HttpServletRequest request, String message) {
        // 每条拒绝都留痕。边缘只认状态码，日志是拒因唯一稳定的出口 ——
        // 响应体随时可能被删改（见 DspVerifyDenyBody 的说明），日志不会。
        // 注意 remote 是 TCP 对端，经网关转发后是网关的地址，不是真实来源
        log.info("DSP 回流拒绝: reason={}, remote={}", message, request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .header("Cache-Control", "no-store")
            .body(new DspVerifyDenyBody(message));
    }
}
