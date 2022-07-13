package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.yuanshen.data.dto.ItemSearchDto;
import site.yuanshen.data.entity.Item;

/**
 * 物品表 Mapper 接口
 *
 * @author Moment
 * @since 2022-06-14 01:45:23
 */
@Mapper
public interface ItemMapper extends BaseMapper<Item> {

    Page<Item> selectPageItem(IPage<?> page, @Param("itemSearchDto") ItemSearchDto itemSearchDto);

}
