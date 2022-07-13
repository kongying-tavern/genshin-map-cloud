package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.yuanshen.data.dto.IconSearchDto;
import site.yuanshen.data.entity.Icon;

/**
 * 图标主表 Mapper 接口
 *
 * @author Moment
 * @since 2022-06-14 01:45:23
 */
@Mapper
public interface IconMapper extends BaseMapper<Icon> {

    Page<Icon> selectPageIcon(IPage<?> page, @Param("searchDto") IconSearchDto searchDto);


}
