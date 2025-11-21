package site.yuanshen.genshin.core.socketIO;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({SocketIOConfiguration.class})
@Slf4j
@RequiredArgsConstructor
public class SocketIONacosDiscovery implements CommandLineRunner, DisposableBean {
    private final SocketIOProperties properties;
    private final NacosDiscoveryProperties discoveryProperties;
    private final static String websocketServiceName = "api-ws";

    @Override
    public void run(String... args) throws Exception {
        try {
            // 构建Nacos服务实例
            Instance instance = new Instance();
            instance.setIp(discoveryProperties.getIp());
            instance.setPort(properties.getPort());
            instance.setHealthy(true);
            // 可以添加元数据以区分
            instance.getMetadata().put("preserved.register.source", "SPRING_CLOUD");
            instance.getMetadata().put("protocol", "netty.socket");

            NamingService namingService = NamingFactory.createNamingService(discoveryProperties.getServerAddr());
            namingService.registerInstance(websocketServiceName, instance);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() throws Exception {
        try {
            NamingService namingService = NamingFactory.createNamingService(discoveryProperties.getServerAddr());
            namingService.deregisterInstance(websocketServiceName, discoveryProperties.getIp(), properties.getPort());
        } catch (NacosException e) {
            System.err.println(e.getMessage());
        }
    }
}
