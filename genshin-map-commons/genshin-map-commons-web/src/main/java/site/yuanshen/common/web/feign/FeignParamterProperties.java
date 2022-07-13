package site.yuanshen.common.web.feign;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * feign统一请求参数
 *
 * @author: Moment
 */
@ConfigurationProperties(prefix = "feign")
@Data
public class FeignParamterProperties {

    /**
     * 需要传递的参数名列表
     */
    private List<String> paramsName;
}
