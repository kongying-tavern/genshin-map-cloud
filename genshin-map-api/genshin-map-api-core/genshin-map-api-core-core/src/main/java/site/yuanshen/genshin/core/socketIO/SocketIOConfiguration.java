package site.yuanshen.genshin.core.socketIO;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SocketIOConfiguration {

    @Value("${socketio.host}")
    private String host;

    @Value("${socketio.port}")
    private Integer port;

    @Value("${socketio.worker-threads}")
    private int workerThreads;

    @Value("${socketio.allow-custom-requests}")
    private boolean allowCustomRequests;

    @Value("${socketio.upgrade-timeout}")
    private int upgradeTimeout;

    @Value("${socketio.ping-timeout}")
    private int pingTimeout;

    @Value("${socketio.ping-interval}")
    private int pingInterval;

    @Value("${socketio.max-frame-payload-length}")
    private int maxFramePayloadLength;

    @Value("${socketio.max-http-content-length}")
    private int maxHttpContentLength;

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        // 配置域名和端口
        config.setHostname(host);
        config.setPort(port);
        // 开启socket端口复用
        com.corundumstudio.socketio.SocketConfig socketConfig = new com.corundumstudio.socketio.SocketConfig();
        socketConfig.setReuseAddress(Boolean.TRUE);
        config.setSocketConfig(socketConfig);
        // 连接数大小
        config.setWorkerThreads(workerThreads);
        // 允许客户请求
        config.setAllowCustomRequests(allowCustomRequests);
        // 协议升级超时时间(毫秒)，默认10秒，HTTP握手升级为ws协议超时时间
        config.setUpgradeTimeout(upgradeTimeout);
        // Ping消息超时时间(毫秒)，默认60秒，这个时间间隔内没有接收到心跳消息就会发送超时事件
        config.setPingTimeout(pingTimeout);
        // Ping消息间隔(毫秒)，默认25秒。客户端向服务器发送一条心跳消息间隔
        config.setPingInterval(pingInterval);
        // 设置HTTP交互最大内容长度
        config.setMaxFramePayloadLength(maxFramePayloadLength);
        // 设置最大每帧处理数据的长度，防止他人利用大数据来攻击服务器
        config.setMaxHttpContentLength(maxHttpContentLength);

        return new SocketIOServer(config);
    }

    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketIOServer) {
        return new SpringAnnotationScanner(socketIOServer());
    }

}
