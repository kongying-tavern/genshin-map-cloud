package site.yuanshen.genshin.core.service;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yuanshen.data.dto.SysUserDeviceDto;
import site.yuanshen.data.entity.SysUserDevice;
import site.yuanshen.genshin.core.dao.SysUserDeviceDao;
import site.yuanshen.genshin.core.utils.ClientUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户设备详情服务
 *
 * @author Alex Fang
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysUserDeviceService {
    private final SysUserDeviceDao sysUserDeviceDao;

    private static final String DEVICE_IP_DEFAULT = "N/A";
    private static final int DEVICE_ID_LEN_LIMIT = 500;

    /**
     * 检查设备是否有登录权限
     */
    public boolean checkDeviceAccess(Long userId) {
        List<SysUserDeviceDto> deviceList = sysUserDeviceDao.getDeviceList(userId.toString());
        String ip = ClientUtils.getClientIpv4(DEVICE_IP_DEFAULT);
        String ua = StrUtil.sub(ClientUtils.getClientUa(), 0, DEVICE_ID_LEN_LIMIT);
        SysUserDeviceDto userDevice = sysUserDeviceDao.createNewDevice(userId, ip, ua);

        SysUserDeviceDto currentDevice = sysUserDeviceDao.findDevice(deviceList, userDevice);
        if(currentDevice == null) {
            currentDevice = sysUserDeviceDao.addNewDevice(userDevice);
        }
        return false;
    }
}
