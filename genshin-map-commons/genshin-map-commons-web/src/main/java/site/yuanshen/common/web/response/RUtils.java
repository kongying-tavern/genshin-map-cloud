package site.yuanshen.common.web.response;

/**
 * 快捷生成R对象的工具方法
 *
 * @author Moment
 */
public class RUtils {

    /**
     * @return 表示成功的R对象
     */
    public static <T> R<T> create(T data) {
        return new R(Codes.SUCCESS, null, null, data);
    }

    /**
     * @param codes 响应码
     * @param message 错误信息
     * @param <T>   泛型
     * @return 指定响应枚举的R对象
     */
    public static <T> R<T> create(Codes codes, String message, Object errData, T data) {
        return new R<T>(codes, message, errData, data);
    }

    /**
     * @param codes 响应码
     * @param message 错误信息
     * @return 指定响应枚举的R对象（不携带任何数据）
     */
    public static R create(Codes codes, String message, Object errData) {
        return new R(codes, message, errData, null);
    }
}
