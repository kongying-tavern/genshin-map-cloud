package site.yuanshen.common.web.response;

/**
 * 快捷生成W对象的工具方法
 *
 * @author Alex Fang
 */
public class WUtils {

    /**
     * @return 表示成功的W对象
     */
    public static <T> W<T> create(String event, T data) {
        return new W(event, data, null);
    }

    /**
     * @return 指定响应枚举的R对象
     */
    public static <T> W<T> create(String event, T data, String message) {
        return new W<T>(event, data, message);
    }
}
