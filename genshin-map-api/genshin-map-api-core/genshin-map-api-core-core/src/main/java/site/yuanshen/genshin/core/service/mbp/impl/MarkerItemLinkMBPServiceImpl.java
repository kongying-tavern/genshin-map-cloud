package site.yuanshen.genshin.core.service.mbp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.MarkerItemLink;
import site.yuanshen.data.mapper.MarkerItemLinkMapper;
import site.yuanshen.genshin.core.service.mbp.MarkerItemLinkMBPService;

/**
 * 点位-物品关联表 Mybatis Plus CRUD服务实现类
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Service
public class MarkerItemLinkMBPServiceImpl extends ServiceImpl<MarkerItemLinkMapper, MarkerItemLink> implements MarkerItemLinkMBPService {

}
