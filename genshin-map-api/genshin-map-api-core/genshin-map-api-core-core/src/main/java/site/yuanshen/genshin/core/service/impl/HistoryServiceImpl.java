package site.yuanshen.genshin.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.mapper.HistoryMapper;
import site.yuanshen.genshin.core.service.HistoryService;

@Service
public class HistoryServiceImpl extends ServiceImpl<HistoryMapper, History> implements HistoryService {
//TODO 此处移至mbp包下，且更名
}


