package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import site.yuanshen.data.dto.TagSearchDto;
import site.yuanshen.data.entity.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图标标签主表 Mapper 接口
 *
 * @since 2023-04-22 12:16:38
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    Page<Tag> selectPageIconTag(IPage<?> page, @Param("tagSearchDto") TagSearchDto tagSearchDto);

}
