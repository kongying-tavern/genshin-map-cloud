package site.yuanshen.genshin.core.socketIO;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class SocketIOProperties {

    @Value("${socketio.host}")
    private String host;

    @Value("${socketio.port}")
    private Integer port;

    @Value("${socketio.context}")
    private String context;

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

    @Value("${socketio.rtt-debounce-gap}")
    private int rttDebounceGap;
}
