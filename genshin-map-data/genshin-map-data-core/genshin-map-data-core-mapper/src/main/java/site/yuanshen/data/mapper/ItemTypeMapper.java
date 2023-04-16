package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import site.yuanshen.data.entity.ItemType;

import java.util.List;

/**
 * 物品类型表 Mapper 接口
 *
 * @author Moment
 * @since 2022-06-14 01:45:23
 */
@Mapper
public interface ItemTypeMapper extends BaseMapper<ItemType> {

    List<ItemType> selectListWithLargeIn(String unnestStr, LambdaQueryWrapper<ItemType> in);

}
