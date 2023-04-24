package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import site.yuanshen.data.dto.ItemSearchDto;
import site.yuanshen.data.entity.Item;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物品表 Mapper 接口
 *
 * @since 2023-04-22 12:16:38
 */
@Mapper
public interface ItemMapper extends BaseMapper<Item> {

    Page<Item> selectPageItem(IPage<?> page, @Param("itemSearchDto") ItemSearchDto itemSearchDto);

}
