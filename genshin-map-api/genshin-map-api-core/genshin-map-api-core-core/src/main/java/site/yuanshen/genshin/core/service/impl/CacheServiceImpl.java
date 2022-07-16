package site.yuanshen.genshin.core.service.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import site.yuanshen.genshin.core.service.CacheService;

/**
 * 缓存服务接口实现
 *
 * @author Alex Fang
 */
@Service
public class CacheServiceImpl implements CacheService {
    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "listItem",allEntries = true),
                    @CacheEvict(value = "listItemType",allEntries = true),
                    @CacheEvict(value = "listItemById",allEntries = true),
            }
    )
    public void cleanItemCache() {
        return;
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "listCommonItem",allEntries = true),
            }
    )
    public void cleanCommonItemCache() {
        return;
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "searchMarkerId",allEntries = true),
                    @CacheEvict(value = "listMarkerById",allEntries = true),
                    @CacheEvict(value = "listMarkerPage",allEntries = true),
            }
    )
    public void cleanMarkerCache() {
        return;
    }
}
