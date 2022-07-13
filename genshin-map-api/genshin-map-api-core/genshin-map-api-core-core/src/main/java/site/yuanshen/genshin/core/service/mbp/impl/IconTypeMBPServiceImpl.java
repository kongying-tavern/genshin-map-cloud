package site.yuanshen.genshin.core.service.mbp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.IconType;
import site.yuanshen.data.mapper.IconTypeMapper;
import site.yuanshen.genshin.core.service.mbp.IconTypeMBPService;

/**
 * 图标分类表 Mybatis Plus CRUD服务实现类
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Service
public class IconTypeMBPServiceImpl extends ServiceImpl<IconTypeMapper, IconType> implements IconTypeMBPService {

}
