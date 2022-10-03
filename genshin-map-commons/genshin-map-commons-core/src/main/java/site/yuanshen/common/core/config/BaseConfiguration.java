package site.yuanshen.common.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 核心commons的基础配置类
 *
 * @author Moment
 */
@SuppressWarnings("SpringComponentScan")
@Configuration
@ComponentScan(basePackages = {
        "site.yuanshen.genshin.core",
        "site.yuanshen.api",
        "site.yuanshen.data.dao",
        "site.yuanshen.common.core.utils",
})
public class BaseConfiguration {

}
