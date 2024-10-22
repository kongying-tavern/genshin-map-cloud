package site.yuanshen.genshin.core.dao;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * 物品信息的数据查询层
 *
 * @author Moment
 */
public interface ItemDao {
    /**
     * 生成物品点位相关信息
     *
     * @param itemIdList 物品ID列表
     * @param itemTypeIdMap 物品类型Map key:item_id, value:item_type_id[]
     * @param itemCountMap 物品计数Map key:item_id, value:{hiddenFlag: count}
     */
    void generateItemMarkerInfo(List<Long> itemIdList, ConcurrentMap<Long, List<Long>> itemTypeIdMap, ConcurrentMap<Long, Map<Integer, Integer>> itemCountMap);

    /**
     * 返回物品分页压缩文档
     *
     * @param flagList 权限标记
     * @param md5 压缩文档数据的MD5
     * @return 压缩后的字节数组
     */
    byte[] getItemBinary(List<Integer> flagList, String md5);

    /**
     * 返回MD5列表
     *
     * @param flagList 权限标记
     * @return 过滤后的MD5数组
     */
    List<String> listItemBinaryMD5(List<Integer> flagList);

    /**
     * 刷新物品压缩缓存并返回压缩文档
     *
     * @return 刷新后的各个分页
     */
    Map<String, byte[]> refreshItemBinaryList();

}
