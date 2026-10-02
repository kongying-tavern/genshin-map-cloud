package site.yuanshen.genshin.core.dsp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code dsp.edge.*} —— 本服务作为【校验接口】的那一组配置。
 *
 * <p>它从 {@link DspProperties} 里分出来，只为一个原因：这一组在 yml 里要写成<b>数组形式</b>
 * —— {@code tokens:} 下挂若干 {@code - "..."}，一个令牌一行。
 *
 * <p>而 {@link DspProperties} 其余字段用的 {@link org.springframework.beans.factory.annotation.Value}
 * 拿不到这种形态：它向 Environment 取的是「单个属性」，YAML 的块状列表会被摊平成
 * {@code dsp.edge.tokens[0]} / {@code [1]} 这些带下标的键，整体 {@code dsp.edge.tokens}
 * 不存在，注入时直接失败。要收 YAML 列表，只能用这里的按名绑定。
 *
 * <p>代价是失去了 {@code @Value} 那种「占位符解析不了就启动失败」的行为：整组漏配时这里
 * 只是几个空列表 / null，不会有任何报错。这一层由 {@link DspProperties#validate()} 在启动期
 * 补回来 —— 空令牌表、未配的时间窗都会在那里抛异常，所以「漏配会启动失败」
 * 这条没有变，只是报错的位置从占位符解析挪到了显式校验，消息也更具体。
 *
 * <p>本组每一键都必须显式配置，<b>一个默认值都不给</b>（详见
 * {@link DspProperties} 类注释里关于默认值的取舍）：它们要么必须与另一个进程里的值对齐
 * （令牌、槽位数），要么是安全参数（时间窗、容差），这两类都不存在「猜一个默认值」的余地。
 */
@Data
@Component
@ConfigurationProperties(prefix = "dsp.edge")
public class DspEdgeProperties {

    /**
     * 校验接口持有的令牌表 {@code tokens[0..slotCount-1]} —— <b>槽位数就是这张表的长度</b>，
     * 每个槽位放一把令牌。
     *
     * <p>叫「槽位数」而不是「令牌数」（虽然两者现在相等）：这个数的作用始终是<b>划分槽位空间</b>
     * —— 本侧拿它当除数 {@code slot = kid mod slotCount}，边缘侧拿它当步长
     * {@code kid = base + n·slotCount}。叫大小描述不了这两个作用，而出错的全是它们：
     * 除数错会验到别的边缘那把令牌，步长错会让 kid 落进别人的槽。
     *
     * <p><b>它必须始终是派生的 —— 只能由 {@code tokens.size()} 得出</b>。不要给它单开一个
     * 配置项：一旦出现第二个事实来源（比如表里有 5 把、却配了 count=8），第 6 槽往后取不到令牌，
     * 而部分边缘的 kid 会被静默映射到错误的槽上 —— 启动期验签一切正常，上线后才发现某几个
     * 节点全军覆没。这也是为什么「每一格都必须有令牌」要写进启动期校验。
     *
     * <p>边缘侧同一个数的环境变量是 {@code DSP_EDGE_SLOT_COUNT}。
     *
     * <p>每个边缘各持一把、互不相同，某把泄露只换那一个槽位、不牵连其余 —— 这是
     * 多边缘 / 多 CDN 厂商下能独立吊销与轮换的前提。另一种模型（两端都持全部槽位的令牌）会让
     * 「单边缘泄露」等于「全部泄露」，本方案不采用。
     *
     * <p>{@code kid} 可以超过槽位数（{@code mod} 后自然落进合法槽位），所以边缘身份与具体
     * 槽位解耦。边缘侧必须持有<b>同一个槽位数</b>（{@code DSP_EDGE_SLOT_COUNT}）以及自己槽位
     * 那把令牌；两处不一致的表现是「该边缘的回流全部验不过」，而且只在它换了槽位后才暴露。
     *
     * <p><b>顺序即 {@code kid} 的槽位</b>，是一条隐式契约：调换两个条目的位置等于把两个边缘的
     * 身份互换，而且没有任何校验会发现 —— 追加新槽只能往表尾加。
     *
     * <p>写成数组而不是逗号串：一条令牌一行，diff 与评审都能看出「加了一个槽」，逗号串里
     * 它们挤在同一行，增删很容易错过。
     */
    private List<String> tokens = new ArrayList<>();

    /**
     * 回流时间戳与本机时间的允许偏差（秒），超出即拒绝 —— 防重放。
     *
     * <p>不给默认值，且<b>这一项不能被省略</b>（见 {@link DspProperties#validate()}）：窗口该是
     * 多少取决于边缘到这里的链路延迟，「漏配即启动失败」比「跑在一个谁也没算过的窗口上」好。
     */
    private Long callbackWindowSeconds;

    /**
     * 判票面是否过期时的时钟容差（秒）：容差越大越宽松，判据是 {@code now - 容差 > exp}。
     *
     * <p>不给默认值。合法取值里本来就有 0（不容差），所以这里用包装类型而不是 {@code long} ——
     * 用基本类型时「省略」会静默变成 0，也就是事实上存在一个默认值。
     *
     * <p>它必须远小于 {@code dsp.signing.expiry-seconds}：容差接近票的寿命，等于把
     * 「这张票能用多久」这件事从此没人管了 —— 这条由 {@link DspProperties#validate()} 判。
     */
    private Long ticketToleranceSeconds;
}
