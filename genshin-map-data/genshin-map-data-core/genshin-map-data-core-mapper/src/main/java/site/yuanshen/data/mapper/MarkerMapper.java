package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.MarkerItemLink;

import java.util.List;

/**
 * 点位主表 Mapper 接口
 *
 * @since 2023-04-22 12:16:38
 */
@Mapper
public interface MarkerMapper extends BaseMapper<Marker> {
    /**
     *  配合in使用
     * @param unnest id in (xxx,xxx) 时所包含的元素 格式为 '{10000, 11000}'
     * @param wrapper
     * @return
     */
    List<Marker> selectListWithLargeIn(@Param("unnest")String unnest, @Param(Constants.WRAPPER)LambdaQueryWrapper<Marker> wrapper);

    /**
     *  配合in使用
     * @param column 字段名
     * @param unnest ${column} in (xxx,xxx) 时所包含的元素 格式为 '{10000, 11000}'
     * @param wrapper
     * @return
     */
    List<Marker> selectWithLargeCustomIn(@Param("column")String column, @Param("unnest")String unnest, @Param(Constants.WRAPPER)LambdaQueryWrapper<Marker> wrapper);

    List<Marker> selectListByMarkerItemLink(@Param("column")String column, @Param("unnest")String unnest, @Param(Constants.WRAPPER)LambdaQueryWrapper<MarkerItemLink> wrapper);

    IPage<Marker> selectPageFilterByHiddenFlag(Page<Marker> page, @Param("hiddenFlags") List<Integer> hiddenFlagList, @Param(Constants.WRAPPER) LambdaQueryWrapper<Marker> wrapper);

    List<Marker> selectListWithLargeInFilterByHiddenFlag(@Param("unnest")String unnest,@Param("hiddenFlags") List<Integer> hiddenFlagList, @Param(Constants.WRAPPER) LambdaQueryWrapper<Marker> wrapper);
}
