package site.yuanshen.genshin.core.service.mbp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.ItemAreaPublic;
import site.yuanshen.data.mapper.ItemAreaPublicMapper;
import site.yuanshen.genshin.core.service.mbp.ItemAreaPublicMBPService;

/**
 * 地区公用物品记录表;创建新地区时直接作为基础关联 Mybatis Plus CRUD服务实现类
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Service
public class ItemAreaPublicMBPServiceImpl extends ServiceImpl<ItemAreaPublicMapper, ItemAreaPublic> implements ItemAreaPublicMBPService {

}
