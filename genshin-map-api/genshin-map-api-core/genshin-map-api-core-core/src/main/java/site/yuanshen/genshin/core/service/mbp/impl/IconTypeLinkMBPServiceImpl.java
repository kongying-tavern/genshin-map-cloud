package site.yuanshen.genshin.core.service.mbp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.IconTypeLink;
import site.yuanshen.data.mapper.IconTypeLinkMapper;
import site.yuanshen.genshin.core.service.mbp.IconTypeLinkMBPService;

/**
 * 图标分类关联表 Mybatis Plus CRUD服务实现类
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Service
public class IconTypeLinkMBPServiceImpl extends ServiceImpl<IconTypeLinkMapper, IconTypeLink> implements IconTypeLinkMBPService {

}
