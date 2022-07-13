package site.yuanshen.common.web.validate;

import site.yuanshen.common.web.utils.ApplicationUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 自定义校验拦截器
 *
 * @author Moment
 */
public class CustomValidHandler implements ConstraintValidator<CustomValid, Object> {

    private CustomValid customValid;

    @Override
    public void initialize(CustomValid constraintAnnotation) {
        this.customValid = constraintAnnotation;
    }

    /**
     * 自定义校验拦截器校验方法
     *
     * @param value   需要校验的对象
     * @param context 校验上下文
     * @return 如果为false则校验不通过
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value != null) {
            //获取自定义验证拦截器的类
            Class<? extends CustomValidHandlerTemplate> handlerCls = customValid.handler();
            //从容器中获取handler
            CustomValidHandlerTemplate handler = ApplicationUtils.getBean(handlerCls);
            //如果handler为空，直接通过当前校验（当前handler不校验）
            if (handler == null) return true;
            //调用校验方法
            return handler.isValid(customValid, value);
        }
        return true;
    }
}
