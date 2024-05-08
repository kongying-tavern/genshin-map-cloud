package site.yuanshen.genshin.core.dao;

import site.yuanshen.data.dto.SysUserDeviceDto;

import java.util.List;

/**
 * 用户设备的数据查询层
 *
 * @author Alex Fang
 */
public interface SysUserDeviceDao {
    /**
     * 获取用户设备列表
     */
    List<SysUserDeviceDto> getDeviceList(String userId);

    /**
     * 创建新设备数据
     */
    SysUserDeviceDto createNewDevice(Long userId, String ipv4, String devId);

    /**
     * 添加新设备
     */
    SysUserDeviceDto addNewDevice(SysUserDeviceDto device);

    /**
     * 在列表查找设备
     */
    SysUserDeviceDto findDevice(List<SysUserDeviceDto> deviceList, SysUserDeviceDto device);
}
