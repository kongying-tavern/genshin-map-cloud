package site.yuanshen.genshin.core.service.mbp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.TagTypeLink;
import site.yuanshen.data.mapper.TagTypeLinkMapper;
import site.yuanshen.genshin.core.service.mbp.TagTypeLinkMBPService;

/**
 * 图标标签分类关联表 Mybatis Plus CRUD服务实现类
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Service
public class TagTypeLinkMBPServiceImpl extends ServiceImpl<TagTypeLinkMapper, TagTypeLink> implements TagTypeLinkMBPService {

}
