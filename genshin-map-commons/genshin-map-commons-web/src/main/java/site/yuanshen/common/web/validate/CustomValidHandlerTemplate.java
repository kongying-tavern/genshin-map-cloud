package site.yuanshen.common.web.validate;

/**
 * 自定义泛型检验接口
 *
 * @param <T> 校验传入对象类型
 */
@FunctionalInterface
public interface CustomValidHandlerTemplate<T> {

    boolean isValid(CustomValid customValid, T value);

}
