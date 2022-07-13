package site.yuanshen.genshin.core.service.mbp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.ItemTypeLink;
import site.yuanshen.data.mapper.ItemTypeLinkMapper;
import site.yuanshen.genshin.core.service.mbp.ItemTypeLinkMBPService;

/**
 * 物品-类型关联表 Mybatis Plus CRUD服务实现类
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Service
public class ItemTypeLinkMBPServiceImpl extends ServiceImpl<ItemTypeLinkMapper, ItemTypeLink> implements ItemTypeLinkMBPService {

}
