package site.yuanshen.common.web.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CustomValidHandler.class)
public @interface CustomValid {

    /**
     * @return 校验失败后的提示信息
     */
    String message() default "校验未通过";

    /**
     * @return 校验分组信息
     */
    Class<?>[] groups() default {};

    /**
     * @return 设置校验的负载 - 原数据
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * @return 自定义的校验借口
     */
    Class<? extends CustomValidHandlerTemplate> handler();

}
