package site.yuanshen.genshin.core.socketIO;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class SocketIOConfiguration {
    private final SocketIOProperties properties;

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        // 配置域名和端口
        config.setHostname(properties.getHost());
        config.setPort(properties.getPort());
        config.setContext("/ws");
        // 开启socket端口复用
        com.corundumstudio.socketio.SocketConfig socketConfig = new com.corundumstudio.socketio.SocketConfig();
        socketConfig.setReuseAddress(Boolean.TRUE);
        config.setSocketConfig(socketConfig);
        // 连接数大小
        config.setWorkerThreads(properties.getWorkerThreads());
        // 允许客户请求
        config.setAllowCustomRequests(properties.isAllowCustomRequests());
        // 协议升级超时时间(毫秒)，默认10秒，HTTP握手升级为ws协议超时时间
        config.setUpgradeTimeout(properties.getUpgradeTimeout());
        // Ping消息超时时间(毫秒)，默认60秒，这个时间间隔内没有接收到心跳消息就会发送超时事件
        config.setPingTimeout(properties.getPingTimeout());
        // Ping消息间隔(毫秒)，默认25秒。客户端向服务器发送一条心跳消息间隔
        config.setPingInterval(properties.getPingInterval());
        // 设置HTTP交互最大内容长度
        config.setMaxFramePayloadLength(properties.getMaxFramePayloadLength());
        // 设置最大每帧处理数据的长度，防止他人利用大数据来攻击服务器
        config.setMaxHttpContentLength(properties.getMaxHttpContentLength());

        return new SocketIOServer(config);
    }

    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketIOServer) {
        return new SpringAnnotationScanner(socketIOServer());
    }

}
