package site.yuanshen.genshin.core.dao;

import site.yuanshen.data.entity.SysUserDevice;

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
    List<SysUserDevice> getDeviceList(String userId);
}
