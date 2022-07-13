package site.yuanshen.genshin.core.service.mbp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.ItemType;
import site.yuanshen.data.mapper.ItemTypeMapper;
import site.yuanshen.genshin.core.service.mbp.ItemTypeMBPService;

/**
 * 物品类型表 Mybatis Plus CRUD服务实现类
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Service
public class ItemTypeMBPServiceImpl extends ServiceImpl<ItemTypeMapper, ItemType> implements ItemTypeMBPService {

}
