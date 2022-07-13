package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import site.yuanshen.data.entity.ItemAreaPublic;

/**
 * 地区公用物品记录表;创建新地区时直接作为基础关联 Mapper 接口
 *
 * @author Moment
 * @since 2022-06-14 01:45:23
 */
@Mapper
public interface ItemAreaPublicMapper extends BaseMapper<ItemAreaPublic> {

}
