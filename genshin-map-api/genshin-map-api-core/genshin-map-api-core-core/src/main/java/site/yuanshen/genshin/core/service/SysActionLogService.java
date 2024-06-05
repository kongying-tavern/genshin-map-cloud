package site.yuanshen.genshin.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.SysActionLog;
import site.yuanshen.data.mapper.SysActionLogMapper;
import site.yuanshen.genshin.core.utils.ClientUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户设备详情服务
 *
 * @author Alex Fang
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysActionLogService {

    private final SysActionLogMapper sysActionLogMapper;

    /**
     * 添加新操作日志
     */
    public void addNewLog(
        Long userId,
        String action,
        boolean isError,
        Map<String, Object> extraData
    ) {
        final ClientUtils.ClientInfo clientInfo = ClientUtils.getClientInfo(null, null);
        addNewLog(userId, action, isError, extraData, clientInfo);
    }

    /**
     * 添加新操作日志
     */
    public void addNewLog(
        Long userId,
        String action,
        boolean isError,
        Map<String, Object> extraData,
        ClientUtils.ClientInfo clientInfo
    ) {
        if(extraData == null) {
            extraData = new HashMap<>();
        }

        final SysActionLog actionLog = new SysActionLog()
            .withUserId(userId)
            .withAction(action)
            .withIpv4(clientInfo.getIpv4())
            .withDeviceId(clientInfo.getUa())
            .withIsError(isError)
            .withExtraData(extraData);

        sysActionLogMapper.insert(actionLog);
    }
}
