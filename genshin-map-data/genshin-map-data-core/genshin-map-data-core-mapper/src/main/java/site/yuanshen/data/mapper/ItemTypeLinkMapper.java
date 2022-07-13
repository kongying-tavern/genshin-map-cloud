package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import site.yuanshen.data.entity.ItemTypeLink;

/**
 * 物品-类型关联表 Mapper 接口
 *
 * @author Moment
 * @since 2022-06-14 01:45:23
 */
@Mapper
public interface ItemTypeLinkMapper extends BaseMapper<ItemTypeLink> {

}
