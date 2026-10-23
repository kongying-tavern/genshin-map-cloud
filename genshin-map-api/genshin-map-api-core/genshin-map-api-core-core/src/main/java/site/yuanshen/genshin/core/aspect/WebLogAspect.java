package site.yuanshen.genshin.core.aspect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author kraken
 * @create 2021/04/30
 */
@Slf4j
@Aspect
@Component
public class WebLogAspect {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    public static final ThreadLocal<StopWatch> STOP_WATCH_THREAD_LOCAL = new ThreadLocal<>();

    public static final JSONWriter.Feature[] defaultWriteFeatures = new JSONWriter.Feature[] {
            JSONWriter.Feature.BrowserCompatible,
            JSONWriter.Feature.WriteEnumUsingToString,
            JSONWriter.Feature.WriteBigDecimalAsPlain,
            JSONWriter.Feature.WriteEnumUsingToString,
            JSONWriter.Feature.WriteNonStringKeyAsString
    };

    /**
     * 敏感字段名关键字（大小写不敏感）：命中后把对应 JSON 值整体替换为 "***"
     */
    private static final Pattern SENSITIVE_JSON_PATTERN = Pattern.compile(
        "(\"[^\"]*(?:password|passwd|token|secret|credential|authorization)[^\"]*\"\\s*:\\s*)"
            + "(?:\"(?:[^\"\\\\]|\\\\.)*\"|[^,}\\]\\s]+)",
        Pattern.CASE_INSENSITIVE
    );

    private static final String MASKED_VALUE = "\"***\"";

    /**
     * 日志脱敏：把 JSON 文本中敏感字段（password/token/secret 等）的值替换为 ***，保持 JSON 结构不变。
     * 仅用于日志输出，不参与任何业务数据。
     */
    static String maskSensitiveJson(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        return SENSITIVE_JSON_PATTERN.matcher(json).replaceAll("$1" + Matcher.quoteReplacement(MASKED_VALUE));
    }

    private Object[] sanitizeArgs(Object[] args) {
        final Object[] sanitizedArgs = List.of(args)
            .parallelStream()
            .map(arg -> {
                if (arg == null) {
                    return null;
                } else if (arg instanceof MultipartFile) {
                    return "@[Instance MultipartFile]";
                } else {
                    return arg;
                }
            })
            .toArray();
        return sanitizedArgs;
    }

    /**
     * 以 controller 包下定义的所有请求为切入点
     */
    @Pointcut("execution(public * site.yuanshen.genshin.core.controller..*.*(..))")
    public void webLog() {
    }

    /**
     * 在切点之前织入
     *
     * @param joinPoint
     * @throws Throwable
     */
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) {
        StopWatch stopWatch = new StopWatch(joinPoint.getSignature().getDeclaringTypeName() + "Controller计时器");
        STOP_WATCH_THREAD_LOCAL.set(stopWatch);
        stopWatch.start(joinPoint.getSignature().getDeclaringTypeName() + "切面开始");
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        String args;
        try {
            // 入参可能含 password/token 等敏感信息，落地日志前统一脱敏
            args = maskSensitiveJson(JSON.toJSONString(sanitizeArgs(joinPoint.getArgs()), defaultWriteFeatures));
        } catch (OutOfMemoryError e) {
            args = "[Too long arguments]";
        }
        log.info(
            LINE_SEPARATOR
                + "URL          : "
                + request.getRequestURL().toString()
                + LINE_SEPARATOR
                + "请求方式     : "
                + request.getMethod()
                + LINE_SEPARATOR
                + "方法名称     : "
                + joinPoint.getSignature().getDeclaringTypeName()
                + "/"
                + joinPoint.getSignature().getName()
                + LINE_SEPARATOR
                + "IP           : "
                + request.getRemoteAddr()
                + LINE_SEPARATOR
                + "请求入参     : "
                + args
                + LINE_SEPARATOR
        );
    }

    /**
     * 在切点之后织入
     *
     * @throws Throwable
     */
    @After("webLog()")
    public void doAfter() {
    }

    /**
     * 环绕
     *
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object result = null;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        try {
            result = proceedingJoinPoint.proceed();
            return result;
        } catch (Exception e) {
            log.info(
                proceedingJoinPoint.getTarget().getClass().getName() + "发生异常msg:{},code:{}",
                e.getMessage()
            );
            throw e;
        } finally {
            STOP_WATCH_THREAD_LOCAL.get().stop();
            String s;
            try {
                s = JSON.toJSONString(result, defaultWriteFeatures);
            } catch (OutOfMemoryError e) {
                s = "[Too large data]";
            }
            log.info(
                LINE_SEPARATOR
                    + "URL          : "
                    + request.getRequestURL().toString()
                    + LINE_SEPARATOR
                    + "请求方式     : "
                    + request.getMethod()
                    + LINE_SEPARATOR
                    + "方法名称     : "
                    + proceedingJoinPoint.getSignature().getDeclaringTypeName()
                    + "/"
                    + proceedingJoinPoint.getSignature().getName() + LINE_SEPARATOR
                    + "请求出参     : "
                    + ((s.length() > 100) ? s.substring(0, 99) : s)
                    + LINE_SEPARATOR
                    + "请求耗时     : "
                    + STOP_WATCH_THREAD_LOCAL.get().getTotalTimeMillis()
                    + " ms"
                    + LINE_SEPARATOR
            );
            STOP_WATCH_THREAD_LOCAL.remove();
        }
    }
}
