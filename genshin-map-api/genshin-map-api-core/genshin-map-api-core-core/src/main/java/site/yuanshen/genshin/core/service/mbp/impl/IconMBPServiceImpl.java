package site.yuanshen.genshin.core.service.mbp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.Icon;
import site.yuanshen.data.mapper.IconMapper;
import site.yuanshen.genshin.core.service.mbp.IconMBPService;

/**
 * 图标主表 Mybatis Plus CRUD服务实现类
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Service
public class IconMBPServiceImpl extends ServiceImpl<IconMapper, Icon> implements IconMBPService {

}
