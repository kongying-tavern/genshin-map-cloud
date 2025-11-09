package site.yuanshen.genshin.core.socketIO;

import cn.hutool.core.util.StrUtil;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.DebounceExecutor;
import site.yuanshen.common.core.utils.TimeUtils;
import site.yuanshen.data.vo.RttCheckVo;

import java.util.concurrent.TimeUnit;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class SocketIOConfiguration {
    private final static String RTT_CHECK_EVENT_NAME = "rttcheck";

    private final SocketIOProperties properties;

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        // 配置域名和端口和握手路径
        config.setHostname(properties.getHost());
        config.setPort(properties.getPort());
        config.setContext(properties.getContext());
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


        SocketIOServer socketIOServer = new SocketIOServer(config);
        socketIOServer.addConnectListener(socketIOClient -> {
            String userId = socketIOClient.getHandshakeData().getSingleUrlParam("userId");
            if (userId == null) {
                // 主动断开连接（服务端会发送错误事件）
                socketIOClient.disconnect();
                throw new GenshinApiException("userId parameter is missing");
            }
        });

        socketIOServer.addEventListener(RTT_CHECK_EVENT_NAME, RttCheckVo.class, (client, data, ackSender) -> {
            String debounceKey = RTT_CHECK_EVENT_NAME + "-" + client.getSessionId().toString();
            DebounceExecutor.debounce(debounceKey, () -> {
                RttCheckVo rttCheckVo = new RttCheckVo();
                rttCheckVo.setReceiveTimestamp(TimeUtils.getCurrentTimestamp().getTime() - properties.getRttDebounceGap());
                rttCheckVo.setId(StrUtil.blankToDefault(data.getId(), ""));
                rttCheckVo.setSendTimestamp(TimeUtils.getCurrentTimestamp().getTime());
                client.sendEvent(RTT_CHECK_EVENT_NAME, rttCheckVo);
            }, properties.getRttDebounceGap(), TimeUnit.MILLISECONDS);
        });
        return socketIOServer;
    }

    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketIOServer) {
        return new SpringAnnotationScanner(socketIOServer());
    }

}
