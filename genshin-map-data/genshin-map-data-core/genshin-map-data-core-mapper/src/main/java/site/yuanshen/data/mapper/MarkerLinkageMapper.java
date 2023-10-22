package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import site.yuanshen.data.entity.MarkerLinkage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 点位关联 Mapper 接口
 *
 * @since 2023-10-21 02:33:40
 */
@Mapper
public interface MarkerLinkageMapper extends BaseMapper<MarkerLinkage> {

    /**
     *  配合in使用
     * @param column 字段名
     * @param unnest ${column} in (xxx,xxx) 时所包含的元素 格式为 '{10000, 11000}'
     * @param wrapper
     * @return
     */
    List<MarkerLinkage> selectWithLargeCustomIn(@Param("column") String column, @Param("type") String type, @Param("unnest") String unnest, @Param(Constants.WRAPPER) LambdaQueryWrapper<MarkerLinkage> wrapper);

    /**
     *  配合in使用
     * @param unnest ${column} in (xxx,xxx) 时所包含的元素 格式为 '{10000, 11000}'
     * @param wrapper
     * @return
     */
    List<MarkerLinkage> selectWithLargeMarkerIdIn(@Param("unnest")String unnest, @Param(Constants.WRAPPER)LambdaQueryWrapper<MarkerLinkage> wrapper);

}
