package site.yuanshen.common.web.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.web.response.Codes;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.common.web.utils.RequestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RestController层异常统一处理
 *
 * @author Moment
 */
@Slf4j
@RestControllerAdvice
public class RestException {

    /**
     * 统一异常处理方法
     */
    @ExceptionHandler(Throwable.class)
    public R exceptionHandler(Throwable t) {
        try {
            //获取当前请求
            HttpServletRequest request = RequestUtils.getHttpServletRequest();
            log.error("[Rest-Exception]-[{}]-message: {}", request.getRequestURL(), t);
            log.debug("[Rest-Exception]-[{}]-debug error message: {} - cause:", request.getRequestURL(), t);
            t.printStackTrace();
        } catch (Exception e) {
            log.error("[Rest-Exception]-message: {}", t.getMessage());
            log.debug("[Rest-Exception]-debug error message:: {} - cause {}", t.getMessage(), t.getStackTrace());
        }
        return RUtils.create(
            Codes.FAIL,
            "请求失败",
            Arrays.stream(t.getStackTrace())
                .map(StackTraceElement::toString)
                .limit(8)
                .collect(Collectors.toList())
        );
    }

    /**
     * 运行时错误处理方法
     * @param e
     * @return
     */
    @ExceptionHandler(GenshinApiException.class)
    public R runtimeExceptionHandler(RuntimeException r) {
        return RUtils.create(Codes.FAIL, r.getMessage(), null);
    }

    /**
     * 对象数据校验异常处理方法
     */
    @ExceptionHandler({
            BindException.class,
            MethodArgumentNotValidException.class})
    public R bindExceptionHandler(Exception e) {
        BindingResult bindingResult = null;
        //取出BindResult
        if (e instanceof BindException)
            bindingResult = ((BindException) e).getBindingResult();
        else if (e instanceof MethodArgumentNotValidException)
            bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
        //创建Response
        assert bindingResult != null;
        return RUtils.create(Codes.PARAMETER_ERROR,
                null,
                bindingResult.getAllErrors().stream()
                        .map(ObjectError::getDefaultMessage)
                        .collect(Collectors.toSet()),
                null);
    }

    /**
     * 形参数据校验异常处理方法
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public R constraintViolationExceptionHandler(ConstraintViolationException e) {
        return RUtils.create(Codes.PARAMETER_ERROR,
                null,
                e.getConstraintViolations().stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.toSet()));
    }
}
