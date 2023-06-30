package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.yuanshen.data.entity.ItemTypeLink;

import java.util.List;

/**
 * 物品-类型关联表 Mapper 接口
 *
 * @since 2023-04-22 12:16:38
 */
@Mapper
public interface ItemTypeLinkMapper extends BaseMapper<ItemTypeLink> {

    List<ItemTypeLink> selectListWithLargeIn(@Param("unnest")String unnest, @Param("ew") LambdaQueryWrapper<ItemTypeLink> wrapper);

    /**
     *  配合in使用
     * @param column 字段名
     * @param unnest ${column} in (xxx,xxx) 时所包含的元素 格式为 '{10000, 11000}'
     * @param wrapper
     * @return
     */
    List<ItemTypeLink> selectWithLargeCustomIn(@Param("column")String column, @Param("unnest")String unnest, @Param("ew")LambdaQueryWrapper<ItemTypeLink> wrapper);

}
