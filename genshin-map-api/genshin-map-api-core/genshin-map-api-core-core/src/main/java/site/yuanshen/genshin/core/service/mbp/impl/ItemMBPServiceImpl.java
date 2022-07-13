package site.yuanshen.genshin.core.service.mbp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.mapper.ItemMapper;
import site.yuanshen.genshin.core.service.mbp.ItemMBPService;

/**
 * 物品表 Mybatis Plus CRUD服务实现类
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Service
public class ItemMBPServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemMBPService {

}
