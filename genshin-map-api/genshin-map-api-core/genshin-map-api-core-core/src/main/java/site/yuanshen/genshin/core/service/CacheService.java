package site.yuanshen.genshin.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import site.yuanshen.common.core.utils.DebounceExecutor;
import site.yuanshen.genshin.core.dao.IconTagDao;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * 缓存服务接口实现
 *
 * @author Alex Fang
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {

    private final MarkerDocService markerDocService;
    private final ItemDocService itemDocService;
    private final IconTagDao iconTagDao;
    private final CacheManager cacheManager;

    ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 20, 200, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(5));

    /**
     * 删除所有标签缓存
     */
    public void cleanIconTagCache() {
        cleanIconTagCache("");
    }

    /**
     * @param tagName 标签名，为空时清除iconTag的所有缓存
     */
    @Caching(
            evict = {
                    @CacheEvict(value = "iconTag", key = "#tagName", condition = "#tagName != null && #tagName != ''", beforeInvocation = true),
                    @CacheEvict(value = "listIconTag", allEntries = true, beforeInvocation = true),
                    @CacheEvict(value = "listAllTag", allEntries = true, beforeInvocation = true),
            }
    )
    public void cleanIconTagCache(String tagName) {
        FutureTask<Status> futureTask = new FutureTask<>(() -> {
            if (StringUtils.isEmpty(tagName)) Objects.requireNonNull(cacheManager.getCache("iconTag")).clear();
            return Status.OK;
        });
        runAfterTransactionByFuture(futureTask);
        try {
            if (futureTask.get() == Status.OK)
                runAfterTransactionDebounceByKey(this::refreshIconTagBz2,
                        FunctionKeyEnum.refreshIconTagBz2,5);
            else
                log.error("cleanIconTagCache执行失败,未知原因");
        } catch (Exception e) {
            log.error("cleanIconTagCache执行失败", e);
        }
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "area", allEntries = true, beforeInvocation = true),
                    @CacheEvict(value = "listArea", allEntries = true, beforeInvocation = true)
            }
    )
    public void cleanAreaCache() {
        log.info("cleanAreaCache");
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "listItem", allEntries = true, beforeInvocation = true),
                    @CacheEvict(value = "listItemType", allEntries = true, beforeInvocation = true),
                    @CacheEvict(value = "listAllItemType", allEntries = true, beforeInvocation = true),
                    @CacheEvict(value = "listItemById", allEntries = true, beforeInvocation = true),
            }
    )
    public void cleanItemCache() {
        runAfterTransactionDebounceByKey(itemDocService::refreshItemBz2MD5,
                FunctionKeyEnum.refreshItemBz2,5);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "listCommonItem", allEntries = true),
            }
    )
    public void cleanCommonItemCache() {
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "searchMarkerId", allEntries = true, beforeInvocation = true),
                    @CacheEvict(value = "listMarkerById", allEntries = true, beforeInvocation = true),
                    @CacheEvict(value = "listMarkerPage", allEntries = true, beforeInvocation = true),
                    @CacheEvict(value = "listMarkerIdRange", allEntries = true, beforeInvocation = true),
            }
    )
    public void cleanMarkerCache() {
        log.info("cleanMarkerCache");
        runAfterTransactionDebounceByKey(markerDocService::refreshMarkerBz2MD5,
                FunctionKeyEnum.refreshMarkerBz2, 5);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "getMarkerLinkageList", allEntries = true, beforeInvocation = true),
                    @CacheEvict(value = "getMarkerLinkageGraph", allEntries = true, beforeInvocation = true)
            }
    )
    public void cleanMarkerLinkageCache() {
        log.info("cleanMarkerLinkageCache");
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "getMarkerLinkagePathCoords", allEntries = true, beforeInvocation = true)
            }
    )
    public void cleanMarkerLinkageMarkerCache() {
        log.info("cleanMarkerLinkageMarkerCache");
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "listNotice", allEntries = true, beforeInvocation = true)
            }
    )
    public void cleanNoticeCache() {
        log.info("cleanNoticeCache");
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "listAllTagBz2", allEntries = true, beforeInvocation = true),
                    @CacheEvict(value = "listAllTagBz2Md5", allEntries = true, beforeInvocation = true)
            }
    )
    public void refreshIconTagBz2() {
        log.info("refreshIconTag");
        iconTagDao.listAllTagBz2Md5();
    }

    enum FunctionKeyEnum {
        refreshIconTagBz2,
        refreshItemBz2,
        refreshMarkerBz2,
    }

    enum Status {
        OK, FAIL
    }

    private void runAfterTransactionDebounceByKey(Runnable r, FunctionKeyEnum keyEnum, int second) {
        DebounceExecutor.debounce(keyEnum.name(), () -> {
            log.info("Debounce Function Run: {}", keyEnum.name());
            try {
                executor.execute(r);
            } catch (RejectedExecutionException e) {
                log.error("线程池拒绝：{}",keyEnum.name());
            }
        }, second, TimeUnit.SECONDS);
    }


    private void runAfterTransactionByFuture(FutureTask<Status> r) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // 当前存在事务
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                public void afterCommit() {
                    executor.execute(r);
                }
            });
        } else { // 当前不存在事务
            executor.execute(r);
        }
    }
}
