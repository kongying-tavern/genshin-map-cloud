package site.yuanshen.genshin.core.service;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.TimeUtils;
import site.yuanshen.data.vo.BinaryMD5Vo;
import site.yuanshen.data.vo.adapter.cache.MarkerLinkageCacheKeyConst;
import site.yuanshen.genshin.core.dao.MarkerLinkageDao;

import java.util.Optional;


/**
 * 点位关联压缩档案服务层实现
 *
 * @author Alex Fang
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MarkerLinkageDocService {
    private final MarkerLinkageDao markerLinkageDao;
    private final CacheManager neverRefreshCacheManager;

    /**
     * 生成点位关联列表的压缩字节数组
     */
    @Cacheable(value = MarkerLinkageCacheKeyConst.MARKER_LINKAGE_LIST_BIN_MD5, cacheManager = "neverRefreshCacheManager")
    public BinaryMD5Vo listMarkerLinkageBinaryMD5() {
        throw new GenshinApiException("缓存未生成或生成失败");
    }

    /**
     * 刷新点位关联列表和对应的md5数组
     *
     * @return 字节数组的md5
     */
    @CachePut(value = MarkerLinkageCacheKeyConst.MARKER_LINKAGE_LIST_BIN_MD5, cacheManager = "neverRefreshCacheManager")
    public BinaryMD5Vo refreshMarkerLinkageListBinaryMD5() {
        final long startTime = System.currentTimeMillis();
        final String result = DigestUtils.md5DigestAsHex(markerLinkageDao.refreshAllMarkerLinkageListBinary());
        CaffeineCache binaryMd5CacheGenerateTimestamp = (CaffeineCache) neverRefreshCacheManager.getCache(MarkerLinkageCacheKeyConst.MARKER_LINKAGE_LIST_BIN_MD5_GENERATE_TIMESTAMP);

        Long time = Optional.ofNullable(binaryMd5CacheGenerateTimestamp.getNativeCache().getIfPresent(""))
            .map(v -> NumberUtils.createLong(v.toString()))
            .orElse(null);

        log.info("点位关联列表MD5生成, cost:{}, result: {}", System.currentTimeMillis() - startTime, JSON.toJSONString(result));
        BinaryMD5Vo binaryMD5Vo = new BinaryMD5Vo();
        binaryMD5Vo.setMd5(result);
        binaryMD5Vo.setTime(time);
        return binaryMD5Vo;
    }

    /**
     * 生成点位关联有向图的压缩字节数组
     */
    @Cacheable(value = MarkerLinkageCacheKeyConst.MARKER_LINKAGE_GRAPH_BIN_MD5, cacheManager = "neverRefreshCacheManager")
    public BinaryMD5Vo graphMarkerLinkageBinaryMD5() {
        throw new GenshinApiException("缓存未生成或生成失败");
    }

    /**
     * 刷新点位关联有向图和对应的md5数组
     *
     * @return 字节数组的md5
     */
    @CachePut(value = MarkerLinkageCacheKeyConst.MARKER_LINKAGE_GRAPH_BIN_MD5, cacheManager = "neverRefreshCacheManager")
    public BinaryMD5Vo refreshMarkerLinkageGraphBinaryMD5() {
        final long startTime = System.currentTimeMillis();
        final String result = DigestUtils.md5DigestAsHex(markerLinkageDao.refreshAllMarkerLinkageGraphBinary());
        CaffeineCache binaryMd5CacheGenerateTimestamp = (CaffeineCache) neverRefreshCacheManager.getCache(MarkerLinkageCacheKeyConst.MARKER_LINKAGE_GRAPH_BIN_MD5_GENERATE_TIMESTAMP);

        Long time = Optional.ofNullable(binaryMd5CacheGenerateTimestamp.getNativeCache().getIfPresent(""))
            .map(v -> NumberUtils.createLong(v.toString()))
            .orElse(null);

        log.info("点位关联有向图MD5生成, cost:{}, result: {}", System.currentTimeMillis() - startTime, JSON.toJSONString(result));
        BinaryMD5Vo binaryMD5Vo = new BinaryMD5Vo();
        binaryMD5Vo.setMd5(result);
        binaryMD5Vo.setTime(time);
        return binaryMD5Vo;
    }
}
