package site.yuanshen.genshin.core.service.mbp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.mapper.MarkerMapper;
import site.yuanshen.genshin.core.service.mbp.MarkerMBPService;

/**
 * 点位主表 Mybatis Plus CRUD服务实现类
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Service
public class MarkerMBPServiceImpl extends ServiceImpl<MarkerMapper, Marker> implements MarkerMBPService {

}
