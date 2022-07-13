package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import site.yuanshen.data.entity.MarkerItemLink;

/**
 * 点位-物品关联表 Mapper 接口
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Mapper
public interface MarkerItemLinkMapper extends BaseMapper<MarkerItemLink> {

}
