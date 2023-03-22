package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.yuanshen.data.dto.ItemSearchDto;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.MarkerItemLink;

import java.util.List;
import java.util.Map;

/**
 * 物品表 Mapper 接口
 *
 * @author Moment
 * @since 2022-06-14 01:45:23
 */
@Mapper
public interface ItemMapper extends BaseMapper<Item> {

    Page<Item> selectPageItem(IPage<?> page, @Param("itemSearchDto") ItemSearchDto itemSearchDto);

    List<Item> selectListWithLargeIn(@Param("unnest")String unnest, @Param("ew") LambdaQueryWrapper<Item> wrapper);

    /**
     *  配合in使用
     * @param column 字段名
     * @param unnest ${column} in (xxx,xxx) 时所包含的元素 格式为 '{10000, 11000}'
     * @param wrapper
     * @return
     */
    List<Item> selectWithLargeCustomIn(@Param("column")String column, @Param("unnest")String unnest, @Param("ew")LambdaQueryWrapper<Item> wrapper);

}
