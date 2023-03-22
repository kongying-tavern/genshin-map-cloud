package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.yuanshen.data.entity.ItemTypeLink;
import site.yuanshen.data.entity.MarkerExtra;

import java.util.List;

/**
 * 点位额外字段表 Mapper 接口
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Mapper
public interface MarkerExtraMapper extends BaseMapper<MarkerExtra> {
    List<MarkerExtra> selectListWithLargeIn(@Param("unnest")String unnest, @Param("ew") LambdaQueryWrapper<MarkerExtra> wrapper);

    /**
     *  配合in使用
     * @param column 字段名
     * @param unnest ${column} in (xxx,xxx) 时所包含的元素 格式为 '{10000, 11000}'
     * @param wrapper
     * @return
     */
    List<MarkerExtra> selectWithLargeCustomIn(@Param("column")String column, @Param("unnest")String unnest, @Param("ew")LambdaQueryWrapper<MarkerExtra> wrapper);
}
