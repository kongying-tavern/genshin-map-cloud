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
        return new R(Codes.SUCCESS.getCode(), Codes.SUCCESS.getMsg(), data);
    }

    /**
     * @param codes 响应码
     * @param <T>   泛型
     * @return 指定响应枚举的R对象
     */
    public static <T> R<T> create(Codes codes, T data) {
        return new R<T>(codes.getCode(), codes.getMsg(), data);
    }

    /**
     * @param codes 响应码
     * @return 指定响应枚举的R对象（不携带任何数据）
     */
    public static R create(Codes codes) {
        return new R(codes.getCode(), codes.getMsg(), null);
    }
}
