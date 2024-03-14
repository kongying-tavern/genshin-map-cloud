package site.yuanshen.genshin.core.websocket;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.yuanshen.common.web.response.W;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
@ServerEndpoint("/ws/{userId}")
public class WebSocketEntrypoint {
    private Session session;

    private String userId;

    private static CopyOnWriteArraySet<WebSocketEntrypoint> webSockets =new CopyOnWriteArraySet<>();
    // 用来存在线连接用户信息
    private static ConcurrentHashMap<String,Session> sessionPool = new ConcurrentHashMap<String, Session>();

    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") String userId) {
        try {
            this.session = session;
            this.userId = userId;
            webSockets.add(this);
            sessionPool.put(userId, session);
            log.info("[websocket] new connection, connection size: " + webSockets.size());
        } catch (Exception e) {
        }
    }

    @OnClose
    public void onClose() {
        try {
            webSockets.remove(this);
            sessionPool.remove(this.userId);
            log.info("[websocket] connection disconnected, connection size: " + webSockets.size());
        } catch (Exception e) {
        }
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("[websocket] message received: " + message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("[websocket] error, cause: " + error.getMessage());
        error.printStackTrace();
    }

    public <T> void broadcast(W<T> message) {
        final String messageText = JSON.toJSONString(message);
        log.info("[websocket] broadcase:" + messageText);
        for(WebSocketEntrypoint webSocket : webSockets) {
            try {
                if(webSocket.session.isOpen() && !Objects.equals(webSocket.userId, this.userId)) {
                    webSocket.session.getAsyncRemote().sendText(messageText);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public <T> void send(String userId, W<T> message) {
        final String messageText = JSON.toJSONString(message);
        Session session = sessionPool.get(userId);
        if (session != null && session.isOpen()) {
            try {
                log.info("[websocket] send message: " + messageText);
                session.getAsyncRemote().sendText(messageText);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public <T> void sendBatch(String[] userIds, W<T> message) {
        final String messageText = JSON.toJSONString(message);
        for (String userId:userIds) {
            Session session = sessionPool.get(userId);
            if (session != null&&session.isOpen()) {
                try {
                    log.info("[websocket] send message: " + messageText);
                    session.getAsyncRemote().sendText(messageText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
