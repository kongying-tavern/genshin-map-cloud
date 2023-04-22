package site.yuanshen.data.mapper;

import site.yuanshen.data.entity.ItemAreaPublic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 地区公用物品记录表;创建新地区时直接作为基础关联 Mapper 接口
 *
 * @since 2023-04-22 12:16:38
 */
@Mapper
public interface ItemAreaPublicMapper extends BaseMapper<ItemAreaPublic> {

}
