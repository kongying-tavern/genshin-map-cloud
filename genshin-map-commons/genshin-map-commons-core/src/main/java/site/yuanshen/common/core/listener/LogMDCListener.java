package site.yuanshen.common.core.listener;

import org.slf4j.MDC;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * 用于往MDC中写入日志参数的监听类
 *
 * @author Moment
 */
public class LogMDCListener implements GenericApplicationListener {

    private static final String APPLICATION_CONFIG_PROPERTIES = "configurationProperties";

    private static final String APP_NAME_PROPERTIES = "spring.application.name";

    /**
     * 用于判断事件类型是否是需要被监听器响应的<br/>
     * 此处对SpringBoot环境参数准备完成的事件进行监听
     *
     * @param eventType the event type (never {@code null})
     */
    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        return eventType.getRawClass() == ApplicationEnvironmentPreparedEvent.class;
    }
    //

    /**
     * supportsEventType返回为True时执行监听器事件
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        //强转数据类型
        ApplicationEnvironmentPreparedEvent preparedEvent = (ApplicationEnvironmentPreparedEvent) event;
        //获取当前应用的参数环境
        ConfigurableEnvironment environment = preparedEvent.getEnvironment();
        //获取参数列表
        MutablePropertySources propertySources = environment.getPropertySources();
        if (!propertySources.contains(APPLICATION_CONFIG_PROPERTIES)) return;
        PropertySource<?> propertySource = propertySources.get(APPLICATION_CONFIG_PROPERTIES);
        if (!propertySource.containsProperty(APP_NAME_PROPERTIES)) return;
        String appName = (String) propertySource.getProperty(APP_NAME_PROPERTIES);
        //将应用名称放入MDC
        MDC.put("appName", appName);
    }

    /**
     * @return 比日志应用监听器更高一级的监听优先级
     */
    @Override
    public int getOrder() {
        return LoggingApplicationListener.DEFAULT_ORDER - 1;
    }
}
