package site.yuanshen.genshin.core.service.mbp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.UserDevice;
import site.yuanshen.data.mapper.UserDeviceMapper;
import site.yuanshen.genshin.core.service.mbp.UserDeviceMBPService;

/**
 * 用户设备 Mybatis Plus CRUD服务实现类
 *
 * @author Alex Fang
 */
@Service
public class UserDeviceMBPServiceImpl extends ServiceImpl<UserDeviceMapper, UserDevice> implements UserDeviceMBPService {

}
