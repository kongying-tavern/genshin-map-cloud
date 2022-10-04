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
     * @return 所有的物品信息的Bz2压缩的md5
     */
    String listAllItemBz2Md5();

}
