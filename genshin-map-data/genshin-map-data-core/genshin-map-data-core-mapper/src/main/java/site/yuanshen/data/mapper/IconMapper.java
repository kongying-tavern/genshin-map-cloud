package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import site.yuanshen.data.dto.IconSearchDto;
import site.yuanshen.data.entity.Icon;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图标主表 Mapper 接口
 *
 * @since 2023-04-22 12:16:38
 */
@Mapper
public interface IconMapper extends BaseMapper<Icon> {

    Page<Icon> selectPageIcon(IPage<?> page, @Param("searchDto") IconSearchDto searchDto);

}
