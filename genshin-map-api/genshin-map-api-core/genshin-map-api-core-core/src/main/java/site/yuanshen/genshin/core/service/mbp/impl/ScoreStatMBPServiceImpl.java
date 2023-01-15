package site.yuanshen.genshin.core.service.mbp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.ScoreStat;
import site.yuanshen.data.mapper.ScoreStatMapper;
import site.yuanshen.genshin.core.service.mbp.ScoreStatMBPService;

/**
 * 评分统计 Mybatis Plus CRUD服务实现类
 *
 * @author Alex Fang
 * @since 2023-01-15 10:30:22
 */
@Service
public class ScoreStatMBPServiceImpl extends ServiceImpl<ScoreStatMapper, ScoreStat>implements ScoreStatMBPService {

}
