package site.yuanshen.genshin.core.aspect;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

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
        StopWatch stopWatch =
                new StopWatch(joinPoint.getSignature().getDeclaringTypeName() + "Controller计时器");
        STOP_WATCH_THREAD_LOCAL.set(stopWatch);
        stopWatch.start(joinPoint.getSignature().getDeclaringTypeName() + "切面开始");
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
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
                        + JSON.toJSONString(joinPoint.getArgs())
                        + LINE_SEPARATOR);
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
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        try {
            result = proceedingJoinPoint.proceed();
            return result;
        } catch (Exception e) {
            log.info(
                    proceedingJoinPoint.getTarget().getClass().getName() + "发生异常msg:{},code:{}",
                    e.getMessage());
            throw e;
        } finally {
            STOP_WATCH_THREAD_LOCAL.get().stop();
            String s = JSON.toJSONString(result);
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
                            + ((s.length()>100)?s.substring(0,99):s)
                            + LINE_SEPARATOR
                            + "请求耗时     : "
                            + STOP_WATCH_THREAD_LOCAL.get().getTotalTimeMillis()
                            + " ms"
                            + LINE_SEPARATOR);
            STOP_WATCH_THREAD_LOCAL.remove();
        }
    }
}