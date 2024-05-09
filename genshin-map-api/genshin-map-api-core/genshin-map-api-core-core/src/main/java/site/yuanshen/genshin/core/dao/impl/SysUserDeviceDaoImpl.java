package site.yuanshen.genshin.core.dao.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.utils.TimeUtils;
import site.yuanshen.data.dto.SysUserDeviceDto;
import site.yuanshen.data.entity.SysUserDevice;
import site.yuanshen.data.enums.DeviceStatusEnum;
import site.yuanshen.data.mapper.SysUserDeviceMapper;
import site.yuanshen.genshin.core.dao.SysUserDeviceDao;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户设备的数据查询层实现
 *
 * @author Alex Fang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserDeviceDaoImpl implements SysUserDeviceDao {
    private final SysUserDeviceMapper sysUserDeviceMapper;

    @Override
    public List<SysUserDeviceDto> getDeviceList(String userId) {
        long userIdVal;
        try {
            userIdVal = Long.parseLong(userId, 10);
        } catch (Exception e) {
            return List.of();
        }
        return sysUserDeviceMapper.selectList(Wrappers.<SysUserDevice>lambdaQuery()
                .eq(SysUserDevice::getUserId, userIdVal)
                .orderByAsc(SysUserDevice::getId))
                .stream()
                .map(SysUserDeviceDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public SysUserDeviceDto createNewDevice(Long userId, String ipv4, String devId) {
        SysUserDevice device = new SysUserDevice()
                .withUserId(userId)
                .withIpv4(ipv4)
                .withDeviceId(devId)
                .withStatus(DeviceStatusEnum.UNKNOWN)
                .withLastLoginTime(null);
        return new SysUserDeviceDto(device);
    }

    @Override
    public SysUserDeviceDto addNewDevice(SysUserDeviceDto deviceDto) {
        if(deviceDto == null || deviceDto.getUserId() == null) {
            return null;
        }
        SysUserDevice device = deviceDto.getEntity();
        sysUserDeviceMapper.insert(device);
        return new SysUserDeviceDto(device);
    }

    @Override
    public SysUserDeviceDto findDevice(List<SysUserDeviceDto> deviceList, SysUserDeviceDto device) {
        if(device == null) {
            return null;
        }
        return deviceList.stream()
                .filter(devItem -> devItem != null && Objects.equals(devItem.getIpv4(), device.getIpv4()) && Objects.equals(devItem.getDeviceId(), device.getDeviceId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void updateDeviceLoginTime(Long id) {
        if(id == null) {
            return;
        }
        sysUserDeviceMapper.update(null, Wrappers.<SysUserDevice>lambdaUpdate()
                .set(SysUserDevice::getLastLoginTime, TimeUtils.getCurrentTimestamp())
                .eq(SysUserDevice::getId, id));
    }
}
