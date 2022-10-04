package site.yuanshen.genshin.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import site.yuanshen.genshin.core.dao.IconTagDao;
import site.yuanshen.genshin.core.dao.ItemDao;
import site.yuanshen.genshin.core.dao.MarkerDao;
import site.yuanshen.genshin.core.service.CacheService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口实现
 *
 * @author Alex Fang
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final MarkerDao markerDao;
    private final ItemDao itemDao;
    private final IconTagDao iconTagDao;
    private final CacheManager cacheManager;

    ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(5));

    /**
     * 删除所有标签缓存
     */
    @Override
    public void cleanIconTagCache() {
        cleanIconTagCache("");
    }

    /**
     * @param tagName 标签名，为空时清除iconTag的所有缓存
     */
    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "iconTag", key = "#tagName", condition = "#tagName != null && #tagName != ''",beforeInvocation = true),
                    @CacheEvict(value = "listIconTag", allEntries = true,beforeInvocation = true),
                    @CacheEvict(value = "listAllTag", allEntries = true,beforeInvocation = true),
                    @CacheEvict(value = "listAllTagBz2", allEntries = true,beforeInvocation = true),
                    @CacheEvict(value = "listAllTagBz2Md5", allEntries = true,beforeInvocation = true)
            }
    )
    public void cleanIconTagCache(String tagName) {
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            // 当前存在事务
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    executor.execute(() -> {
                        if (StringUtils.isEmpty(tagName)) Objects.requireNonNull(cacheManager.getCache("iconTag")).clear();
                        iconTagDao.listAllTagBz2Md5();
                    });
                }});
        } else {
            // 当前不存在事务
            executor.execute(() ->{
                if (StringUtils.isEmpty(tagName)) Objects.requireNonNull(cacheManager.getCache("iconTag")).clear();
                iconTagDao.listAllTagBz2Md5();
            });
        }
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "listItem",allEntries = true,beforeInvocation = true),
                    @CacheEvict(value = "listItemType",allEntries = true,beforeInvocation = true),
                    @CacheEvict(value = "listItemById",allEntries = true,beforeInvocation = true),
                    @CacheEvict(value = "listAllItemBz2",allEntries = true,beforeInvocation = true),
                    @CacheEvict(value = "listAllItemBz2Md5",allEntries = true,beforeInvocation = true),
            }
    )
    public void cleanItemCache() {

        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            // 当前存在事务
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    executor.execute(() -> {
                        itemDao.listAllItemBz2();
                        itemDao.listAllItemBz2Md5();
                    });
                }});
        } else {
            // 当前不存在事务
            executor.execute(() ->{
                itemDao.listAllItemBz2();
                itemDao.listAllItemBz2Md5();
            });
        }


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
                    @CacheEvict(value = "listMarkerIdRange",allEntries = true,beforeInvocation = true),
                    @CacheEvict(value = "listPageMarkerByBz2",allEntries = true,beforeInvocation = true),
                    @CacheEvict(value = "listMarkerBz2MD5",allEntries = true,beforeInvocation = true),
            }
    )
    public void cleanMarkerCache() {
        log.debug("cleanMarkerCache");
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            // 当前存在事务
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    executor.execute(() -> markerDao.listMarkerBz2MD5(false));
                }});
        } else {
            // 当前不存在事务
            executor.execute(() -> markerDao.listMarkerBz2MD5(false));
        }

    }
}
