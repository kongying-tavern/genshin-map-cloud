package site.yuanshen.genshin.core.dsp.verify;

/**
 * 拒绝时的响应体 —— 只有一个给人读的中文说明，没有机器码。
 *
 * <p>为什么放响应体而不是响应头：人对着 curl 调试时，看 body 比看 header 直接；而自定义响应头
 * 还要额外约定命名空间（{@code X-*} 头在链路上被代理改写的事并不少见）。放 body 则不需要任何
 * 约定 —— 它是一个普通的 JSON 对象，谁都认识。
 *
 * <p><b>这个字段不是契约的一部分</b>：边缘只按状态码办事，不得依赖它的内容或是否存在 ——
 * 哪天把它删掉、或者换个说法，都不需要改边缘。它唯一的读者是人。
 */
public class DspVerifyDenyBody {

    private final String message;

    public DspVerifyDenyBody(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
