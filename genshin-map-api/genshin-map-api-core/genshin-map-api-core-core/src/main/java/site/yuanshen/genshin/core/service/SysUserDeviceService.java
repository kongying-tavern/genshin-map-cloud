package site.yuanshen.genshin.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户设备详情服务
 *
 * @author Alex Fang
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysUserDeviceService {
    /**
     * 检查设备是否有登录权限
     */
    public boolean checkDeviceAccess() {
        return false;
    }
}
