package site.yuanshen.genshin.core.service.mbp.impl;

import site.yuanshen.data.entity.Route;
import site.yuanshen.data.mapper.RouteMapper;
import site.yuanshen.api.core.service.mbp.RouteMBPService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 路线 Mybatis Plus CRUD服务实现类
 *
 * @author Moment
 * @since 2023-01-03 04:31:56
 */
@Service
public class RouteMBPServiceImpl extends ServiceImpl<RouteMapper, Route> implements RouteMBPService {

}
