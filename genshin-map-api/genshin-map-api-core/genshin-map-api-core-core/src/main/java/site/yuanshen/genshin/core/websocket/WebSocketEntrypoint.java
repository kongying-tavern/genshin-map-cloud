package site.yuanshen.genshin.core.websocket;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.yuanshen.common.web.response.W;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Objects;
import java.util.Set;
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

    /**
     * 广播消息
     * @param userId 用户ID，如果用户ID存在，则不对指定ID进行广播，如果用户ID为null则广播给所有人
     * @param message 用户消息
     */
    public <T> void broadcast(String userId, W<T> message) {
        final String messageText = JSON.toJSONString(message);
        log.info("[websocket] broadcast:" + messageText);
        for (WebSocketEntrypoint webSocket : webSockets) {
            try {
                if(webSocket.session.isOpen()) {
                    if (userId != null && Objects.equals(userId, webSocket.userId)) {
                        return;
                    }
                    webSocket.session.getAsyncRemote().sendText(messageText);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送给指定用户ID频道
     * @param userIds 接收者用户ID
     * @param message 用户消息
     */
    public <T> void sendToUsers(String[] userIds, W<T> message) {
        final String messageText = JSON.toJSONString(message);
        for (String userId : userIds) {
            Session session = sessionPool.get(userId);
            if (session != null && session.isOpen()) {
                try {
                    log.info("[websocket] send message to users (" + userId + "): " + messageText);
                    session.getAsyncRemote().sendText(messageText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 发送给所有人，但排除部分用户ID
     * @param userIds 需要排除的用户ID，列表中的用户无法接收到消息
     * @param message 用户消息
     */
    public <T> void sendExceptUsers(String[] userIds, W<T> message) {
        final String messageText = JSON.toJSONString(message);
        final Set<String> userIdSet = Set.of(userIds);
        for (WebSocketEntrypoint webSocket : webSockets) {
            try {
                if(webSocket.session.isOpen()) {
                    if (webSocket.userId != null && userIdSet.contains(webSocket.userId)) {
                        return;
                    }
                    log.info("[websocket] send message to users (" + webSocket.userId + "): " + messageText);
                    webSocket.session.getAsyncRemote().sendText(messageText);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
