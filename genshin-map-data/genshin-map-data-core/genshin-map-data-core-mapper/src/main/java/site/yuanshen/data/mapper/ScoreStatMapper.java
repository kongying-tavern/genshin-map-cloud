package site.yuanshen.data.mapper;

import site.yuanshen.data.entity.ScoreStat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评分统计 Mapper 接口
 *
 * @author Alex Fang
 * @since 2023-01-15 10:30:22
 */
@Mapper
public interface ScoreStatMapper extends BaseMapper<ScoreStat> {

}
