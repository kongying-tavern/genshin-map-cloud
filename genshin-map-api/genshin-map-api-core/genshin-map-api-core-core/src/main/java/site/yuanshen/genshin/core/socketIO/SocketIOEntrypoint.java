package site.yuanshen.genshin.core.socketIO;

import com.alibaba.fastjson2.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.yuanshen.common.web.response.W;
import site.yuanshen.data.enums.SocketIOEventEnum;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SocketIOEntrypoint {
    @Resource
    private SocketIOServer socketIOServer;

    private static ConcurrentHashMap<String, Set<UUID>> userIdToSocketClientUUID = new ConcurrentHashMap<>();

    @OnConnect
    public void onConnect(SocketIOClient socketIOClient) {
        String userId = socketIOClient.getHandshakeData().getSingleUrlParam("userId");
        UUID sessionId = socketIOClient.getSessionId();

        userIdToSocketClientUUID.compute(userId, (uId, uuidList) -> {
            if (uuidList == null) {
                uuidList = new HashSet<>();
            }
            uuidList.add(sessionId);
            return uuidList;
        });
        log.info("[websocket] new connection, userID:{}, socketClientUUID:{}, connection size:{} ", userId, sessionId, socketIOServer.getAllClients().size());
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        UUID sessionId = client.getSessionId();

        userIdToSocketClientUUID.compute(userId, (uId, uuidList) -> {
            if (uuidList == null) {
                uuidList = new HashSet<>();
            }
            uuidList.remove(sessionId);
            return uuidList;
        });
        log.info("[websocket] connection disconnected, userID:{}, socketClientUUID:{}, connection size:{} ", userId, sessionId, socketIOServer.getAllClients().size());
    }

    public <T> void broadcast(W<T> message) {
        String messageText = JSON.toJSONString(message);
        log.info("[websocket] broadcast:" + messageText);
        socketIOServer.getBroadcastOperations().sendEvent(SocketIOEventEnum.MESSAGE.getEvent(), messageText);
    }

    public <T> void sendToUsers(String[] userIds, W<T> message) {
        final String messageText = JSON.toJSONString(message);
        for (String userId : userIds) {
            Set<UUID> uuidList = userIdToSocketClientUUID.get(userId);
            if (uuidList == null) {
                continue;
            }
            uuidList.forEach(uuid -> {
                log.info("[websocket] send message to users, userID:{}, socketClientUUID:{} , message:{}", userId, uuid, messageText);
                socketIOServer.getClient(uuid).sendEvent(SocketIOEventEnum.MESSAGE.getEvent(), messageText);
            });
        }
    }

    public <T> void sendExceptUsers(String[] userIds, W<T> message) {
        final String messageText = JSON.toJSONString(message);
        Set<String> userIdsSet = Set.of(userIds);
        userIdToSocketClientUUID.forEach((userId, uuidList) -> {
            if (!userIdsSet.contains(userId)) {
                uuidList.forEach(uuid -> {
                    log.info("[websocket] send message to users, userID:{}, socketClientUUID:{} , message:{}", userId, uuid, messageText);
                    socketIOServer.getClient(uuid).sendEvent(SocketIOEventEnum.MESSAGE.getEvent(), messageText);
                });
            }
        });
    }
}
