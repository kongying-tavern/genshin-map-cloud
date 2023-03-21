package site.yuanshen.genshin.core.service;

/**
 * 物品压缩档案服务层接口
 *
 * @author Moment
 */
public interface ItemDocService {
    /**
     * 生成物品的bz2压缩字节数组
     * @return 字节数组的md5
     */
    String listItemBz2MD5();

    /**
     * 刷新物品分页bz2和对应的md5数组
     * @return 字节数组的md5
     */
    String refreshItemBz2MD5();

}
