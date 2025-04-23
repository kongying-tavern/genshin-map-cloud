package site.yuanshen.genshin.core.socketIO;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SocketIOConfiguration {

    @Value("${socketio.host:127.0.0.1}")
    private String host;

    @Value("${socketio.port:3000}")
    private Integer port;

    @Value("${socketio.workerThreads:100}")
    private int workerThreads;

    @Value("${socketio.allowCustomRequests:true}")
    private boolean allowCustomRequests;

    @Value("${socketio.upgradeTimeout:1000000}")
    private int upgradeTimeout;

    @Value("${socketio.pingTimeout:6000000}")
    private int pingTimeout;

    @Value("${socketio.pingInterval:25000}")
    private int pingInterval;

    @Value("${socketio.maxFramePayloadLength:1048576}")
    private int maxFramePayloadLength;

    @Value("${socketio.maxHttpContentLength:1048576}")
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
        return new SpringAnnotationScanner(socketIOServer);
    }

}
