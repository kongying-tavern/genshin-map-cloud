package site.yuanshen.genshin.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import site.yuanshen.data.dao.MarkerDao;
import site.yuanshen.genshin.core.service.CacheService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 缓存服务接口实现
 *
 * @author Alex Fang
 */
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final MarkerDao markerDao;
    private final CacheManager cacheManager;

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "listItem",allEntries = true),
                    @CacheEvict(value = "listItemType",allEntries = true),
                    @CacheEvict(value = "listItemById",allEntries = true),
            }
    )
    public void cleanItemCache() {
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "listCommonItem",allEntries = true),
            }
    )
    public void cleanCommonItemCache() {
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "searchMarkerId",allEntries = true,beforeInvocation = true),
                    @CacheEvict(value = "listMarkerById",allEntries = true,beforeInvocation = true),
                    @CacheEvict(value = "listMarkerPage",allEntries = true,beforeInvocation = true),
                    @CacheEvict(value = "getMarkerCount",allEntries = true,beforeInvocation = true),
                    @CacheEvict(value = "listPageMarkerByBz2",allEntries = true,beforeInvocation = true),
                    @CacheEvict(value = "listMarkerBz2MD5",allEntries = true,beforeInvocation = true),
            }
    )
    public void cleanMarkerCache() {
        long totalPages = (markerDao.getMarkerCount(false) + 3000 - 1) / 3000;
        List<Integer> indexList = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            indexList.add(i);
        }
        indexList.parallelStream().forEach(i -> markerDao.listPageMarkerByBz2(false, (long) i));
        markerDao.listMarkerBz2MD5(false);
    }
}
