package site.yuanshen.genshin.core.dao;

import site.yuanshen.data.vo.ItemVo;

import java.util.List;

/**
 * TODO
 *
 * @author Moment
 */
public interface ItemDao {

    /**
     * @return 所有的物品信息
     */
    List<ItemVo> listAllItem();

    /**
     * @return 所有的物品信息的Bz2压缩
     */
    byte[] listAllItemBz2();

    /**
     * 刷新物品压缩缓存并返回压缩文档
     *
     * @return 物品压缩文档
     */
    String refreshAllItemBz2();

}
