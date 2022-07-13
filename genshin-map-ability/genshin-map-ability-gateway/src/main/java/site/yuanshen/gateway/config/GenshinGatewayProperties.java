package site.yuanshen.gateway.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "genshin")
@Data
public class GenshinGatewayProperties {

    private Map<String, List<String>> authoritiesFilter;

}
