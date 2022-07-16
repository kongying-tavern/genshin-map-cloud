package site.yuanshen.genshin.core.service;

/**
 * 缓存服务接口
 *
 * @author Alex Fang
 */
public interface CacheService {

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
