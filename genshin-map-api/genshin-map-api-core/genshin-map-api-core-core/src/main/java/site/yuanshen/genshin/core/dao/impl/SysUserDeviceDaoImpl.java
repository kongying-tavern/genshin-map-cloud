package site.yuanshen.genshin.core.dao.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.SysUserDevice;
import site.yuanshen.data.mapper.SysUserDeviceMapper;
import site.yuanshen.genshin.core.dao.SysUserDeviceDao;

import java.util.List;

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
    public List<SysUserDevice> getDeviceList(String userId) {
        long userIdVal;
        try {
            userIdVal = Long.parseLong(userId, 10);
        } catch (Exception e) {
            return List.of();
        }
        return sysUserDeviceMapper.selectList(Wrappers.<SysUserDevice>lambdaQuery()
                .eq(SysUserDevice::getId, userIdVal));
    }
}
