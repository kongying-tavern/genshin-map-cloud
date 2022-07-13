package site.yuanshen.common.web.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import site.yuanshen.common.web.exception.RestException;
import site.yuanshen.common.web.utils.ApplicationUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Web模块自动装配核心配置
 *
 * @author Moment
 */
@Configuration
public class BaseConfiguration {

    /**
     * 相关基础、工具配置类
     */
    @Configuration
    public static class BaseAutoConfiguration {
        /**
         * 装配Application工具类
         */
        @Bean
        public ApplicationUtils getApplicationUtils() {
            return new ApplicationUtils();
        }
    }

    /**
     * Web相关的配置依赖
     */
    @Configuration
    public static class WebMvcConfiguration {
        /**
         * @return RestController层异常统一处理类
         */
        @Bean
        public RestException getRestException() {
            return new RestException();
        }
    }

    /**
     * 缓存配置
     */
    @Configuration
    @EnableCaching
    public static class CacheConfiguration {

        /**
         * 咖啡因缓存配置
         */
        @Bean
        @Primary
        public CacheManager cacheManager() {
            CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
            Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                    // 设置最后一次写入或访问后经过固定时间过期
                    .expireAfterAccess(60, TimeUnit.MINUTES);
            caffeineCacheManager.setCaffeine(caffeine);
            return caffeineCacheManager;
        }

    }


    /**
     * nacos 注册中心相关的配置
     */
    @Configuration
    @EnableDiscoveryClient
    public static class NacosConfiguration {
        /**
         * 配置相关的微服务的元数据数信息
         */
        @Bean
        @Primary
        public NacosDiscoveryProperties getNacosDiscoverProperties() {
            NacosDiscoveryProperties nacosDiscoveryProperties = new NacosDiscoveryProperties();
            //设置相关的元数据信息
            Map<String, String> map = new HashMap<>();
            map.put("online.time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            nacosDiscoveryProperties.setMetadata(map);
            return nacosDiscoveryProperties;
        }
    }

}
