package site.yuanshen.genshin.core.service;

/**
 * 缓存服务接口
 *
 * @author Alex Fang
 */
public interface CacheService {

    /**
     * 删除所有标签缓存
     */
    void cleanIconTagCache();

    /**
     * 删除指定标签缓存
     * @param tagName 标签名，为空时清除iconTag的所有缓存
     */
    void cleanIconTagCache(String tagName);

    /**
     * 删除物品缓存
     */
    void cleanItemCache();

    /**
     * 删除公用物品缓存
     */
    void cleanCommonItemCache();

    /**
     * 删除点位缓存
     */
    void cleanMarkerCache();
}
