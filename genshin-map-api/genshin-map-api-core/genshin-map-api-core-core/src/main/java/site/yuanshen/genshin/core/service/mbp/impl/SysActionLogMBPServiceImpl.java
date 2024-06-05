package site.yuanshen.genshin.core.service.mbp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.SysActionLog;
import site.yuanshen.data.mapper.SysActionLogMapper;
import site.yuanshen.genshin.core.service.mbp.SysActionLogMBPService;

/**
 * 系统操作日志表;系统操作日志 Mybatis Plus CRUD服务实现类
 *
 * @author Alex Fang
 */
@Service
public class SysActionLogMBPServiceImpl extends ServiceImpl<SysActionLogMapper, SysActionLog> implements SysActionLogMBPService {

}
