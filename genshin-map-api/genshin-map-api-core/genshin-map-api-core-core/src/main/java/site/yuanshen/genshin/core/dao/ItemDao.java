package site.yuanshen.genshin.core.dao;

import site.yuanshen.data.vo.ItemVo;

import java.util.List;

/**
 * 物品信息的数据查询层
 *
 * @author Moment
 */
public interface ItemDao {

    /**
     * @return 所有的物品信息
     */
    List<ItemVo> listAllItem();

    /**
     * @return 所有的物品信息的压缩
     */
    byte[] listAllItemBinary();

    /**
     * 刷新物品压缩缓存并返回压缩文档
     *
     * @return 物品压缩文档
     */
    byte[] refreshAllItemBinary();

}
