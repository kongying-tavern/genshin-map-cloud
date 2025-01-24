package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import site.yuanshen.data.entity.History;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 历史操作表 Mapper 接口
 *
 * @since 2023-04-22 12:16:38
 */
@Mapper
public interface HistoryMapper extends BaseMapper<History> {
    List<History> selectListWithLargeIn(@Param("unnest")String unnest, @Param(Constants.WRAPPER) LambdaQueryWrapper<History> wrapper);

    /**
     *  配合in使用
     * @param column 字段名
     * @param unnest ${column} in (xxx,xxx) 时所包含的元素 格式为 '{10000, 11000}'
     * @param wrapper
     * @return
     */
    List<History> selectWithLargeCustomIn(@Param("column")String column, @Param("unnest")String unnest, @Param(Constants.WRAPPER)LambdaQueryWrapper<History> wrapper);
}
