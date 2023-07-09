package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.yuanshen.data.entity.MarkerItemLink;

import java.util.List;

/**
 * 点位-物品关联表 Mapper 接口
 *
 * @since 2023-04-22 12:16:38
 */
@Mapper
public interface MarkerItemLinkMapper extends BaseMapper<MarkerItemLink> {

    List<MarkerItemLink> selectListWithLargeIn(@Param("unnest")String unnest, @Param(Constants.WRAPPER) LambdaQueryWrapper<MarkerItemLink> wrapper);

    /**
     *  配合in使用
     * @param column 字段名
     * @param unnest ${column} in (xxx,xxx) 时所包含的元素 格式为 '{10000, 11000}'
     * @param wrapper
     * @return
     */
    List<MarkerItemLink> selectWithLargeCustomIn(@Param("column")String column, @Param("unnest")String unnest, @Param(Constants.WRAPPER)LambdaQueryWrapper<MarkerItemLink> wrapper);

}
